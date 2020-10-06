package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.cmtech.android.bledevice.record.IDiagnosable.*;
import static com.cmtech.android.bledevice.report.EcgReport.DONE;
import static com.cmtech.android.bledevice.report.EcgReport.PROCESS;
import static com.cmtech.android.bledevice.report.EcgReport.REQUEST;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.RETURN_CODE_SUCCESS;

public class RecordReportLayout extends LinearLayout {
    public static final int TITLE_ID = R.string.report_title;
    private BleEcgRecord10 record;

    private TextView tvTime;
    private EditText etContent;
    private TextView tvStatus;
    private Button btnRequest;
    private Button btnGet;

    public RecordReportLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_report, this);

        etContent = view.findViewById(R.id.et_report_content);
        tvTime = view.findViewById(R.id.tv_report_time);
        tvStatus = view.findViewById(R.id.tv_report_status);

        IWebCallback requestReportWebCallback = new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                Context context = RecordReportLayout.this.getContext();
                if(code != RETURN_CODE_SUCCESS) {
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

        btnGet = view.findViewById(R.id.btn_get_report);
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFromWeb();
            }
        });

        btnRequest = view.findViewById(R.id.btn_request_report);
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(record != null)
                    record.requestDiagnose(getContext(), requestReportWebCallback);
            }
        });
    }

    public void setRecord(BleEcgRecord10 record) {
        this.record = record;
        updateView();
    }

    public void updateFromWeb() {
        if(record != null) {
            IWebCallback getReportWebCallback = new IWebCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    Context context = RecordReportLayout.this.getContext();
                    if(code == RETURN_CODE_SUCCESS) {
                        JSONObject reportResult = (JSONObject) result;
                        try {
                            int reportCode = reportResult.getInt("reportCode");
                            switch (reportCode) {
                                case CODE_REPORT_SUCCESS:
                                    if (reportResult.has("report")) {
                                        long time = reportResult.getJSONObject("report").getLong("reportTime");
                                        boolean updated = (record.getReport().getReportTime() < time);
                                        record.getReport().fromJson(reportResult.getJSONObject("report"));
                                        record.save();
                                        updateView();
                                        if (updated) {
                                            Toast.makeText(context, "报告已更新", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    break;
                                case CODE_REPORT_FAILURE:
                                    Toast.makeText(context, "获取报告错误", Toast.LENGTH_SHORT).show();
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
                }
            };

            record.retrieveDiagnoseResult(getContext(), getReportWebCallback);
        }
    }

    public void updateView() {
        if(record == null || record.getReport() == null) return;
        long time = record.getReport().getReportTime();
        if(time <= 0) return;

        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        tvTime.setText(dateFmt.format(time));

        String statusStr = "未知";
        switch (record.getReport().getStatus()) {
            case DONE:
                statusStr = "已诊断";
                break;
            case REQUEST:
                statusStr = "等待处理";
                break;
            case PROCESS:
                statusStr = "正在处理";
                break;
            default:
                break;
        }
        tvStatus.setText(statusStr);

        etContent.setText(record.getReport().getContent());

    }
}
