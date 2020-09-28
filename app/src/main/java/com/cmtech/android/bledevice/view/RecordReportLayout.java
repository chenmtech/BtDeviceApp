package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebOperationCallback;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;

import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_ADD_NEW;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_FAILURE;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_NO_NEW;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_PROCESSING;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_REQUEST_AGAIN;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_SUCCESS;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_SUCCESS;

public class RecordReportLayout extends LinearLayout {
    public static final int TITLE_ID = R.string.report_title;
    private BleEcgRecord10 record;

    private EditText etContent;
    private Button btnRequest;
    private Button btnGet;

    public RecordReportLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_report, this);

        etContent = view.findViewById(R.id.et_ecg_report);

        IWebOperationCallback requestReportWebCallback = new IWebOperationCallback() {
            @Override
            public void onFinish(int code, Object result) {
                Context context = RecordReportLayout.this.getContext();
                if(code != WEB_CODE_SUCCESS) {
                    Toast.makeText(context, R.string.web_failure, Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject reportResult = (JSONObject)result;
                try {
                    int reportCode = reportResult.getInt("reportCode");
                    switch (reportCode) {
                        case CODE_REPORT_FAILURE:
                            Toast.makeText(context, "获取报告错误", Toast.LENGTH_SHORT).show();
                            break;
                        case CODE_REPORT_ADD_NEW:
                            Toast.makeText(context, "已申请新的报告", Toast.LENGTH_SHORT).show();
                            break;
                        case CODE_REPORT_PROCESSING:
                            Toast.makeText(context, "报告处理中，请等待", Toast.LENGTH_SHORT).show();
                            break;
                        case CODE_REPORT_REQUEST_AGAIN:
                            Toast.makeText(context, "已重新申请报告", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        IWebOperationCallback getReportWebCallback = new IWebOperationCallback() {
            @Override
            public void onFinish(int code, Object result) {
                Context context = RecordReportLayout.this.getContext();
                if(code != WEB_CODE_SUCCESS) {
                    Toast.makeText(context, R.string.web_failure, Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject reportResult = (JSONObject)result;
                try {
                    int reportCode = reportResult.getInt("reportCode");
                    switch (reportCode) {
                        case CODE_REPORT_SUCCESS:
                            if(reportResult.has("report")) {
                                long time = reportResult.getJSONObject("report").getLong("reportTime");
                                if(record.getReport().getReportTime() < time) {
                                    record.getReport().fromJson(reportResult.getJSONObject("report"));
                                    record.save();
                                    ViseLog.e(record.getReport());
                                    etContent.setText(record.getReport().toString());
                                }
                            }
                            break;
                        case CODE_REPORT_FAILURE:
                            Toast.makeText(context, "请求报告错误", Toast.LENGTH_SHORT).show();
                            break;
                        case CODE_REPORT_NO_NEW:
                            Toast.makeText(context, "暂无新报告", Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        btnGet = view.findViewById(R.id.btn_get_report);
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(record != null)
                    record.downloadReport(getContext(), getReportWebCallback);
            }
        });

        btnRequest = view.findViewById(R.id.btn_request_report);
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(record != null)
                    record.requestReport(getContext(), requestReportWebCallback);
            }
        });
    }

    public void setRecord(BleEcgRecord10 record) {
        this.record = record;
    }

    public void update() {
        if(record != null && record.getReport() != null)
            etContent.setText(record.getReport().toString());
    }

}
