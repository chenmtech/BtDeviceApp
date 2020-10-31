package com.cmtech.android.bledeviceapp.view.layout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.WebResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.cmtech.android.bledeviceapp.data.record.IDiagnosable.CODE_REPORT_FAILURE;
import static com.cmtech.android.bledeviceapp.data.record.IDiagnosable.CODE_REPORT_NO_NEW;
import static com.cmtech.android.bledeviceapp.data.record.IDiagnosable.CODE_REPORT_SUCCESS;
import static com.cmtech.android.bledeviceapp.data.report.EcgReport.INVALID_TIME;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class RecordReportLayout extends LinearLayout {
    private BleEcgRecord record;

    private final TextView tvAveHr;
    private final TextView tvContent;
    private final TextView tvTime;
    private final TextView tvReportVer;

    public RecordReportLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_report, this);

        tvAveHr = view.findViewById(R.id.tv_report_ave_hr);
        tvContent = view.findViewById(R.id.tv_report_content);
        tvTime = view.findViewById(R.id.tv_report_time);
        tvReportVer = view.findViewById(R.id.tv_report_ver);

        ImageButton ibRefresh = view.findViewById(R.id.ib_request_report);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(record != null) {
                    if(MyApplication.getEcgAlgorithm().getVer().compareTo(record.getReport().getVer()) > 0) {
                        record.requestDiagnose();
                        updateView();
                        Toast.makeText(getContext(), "已更新诊断结果", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "当前报告已是最新版本的。", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void setRecord(BleEcgRecord record) {
        this.record = record;
    }

    public void updateView() {
        if(record == null || record.getReport() == null) return;

        long time = record.getReport().getReportTime();
        if(time > INVALID_TIME) {
            DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            tvTime.setText(dateFmt.format(time));
            tvAveHr.setText(String.valueOf(record.getReport().getAveHr()));
            tvContent.setText(record.getReport().getContent());
            tvReportVer.setText(record.getReport().getVer());
        } else {

        }
    }

    public void updateFromWeb() {
        if(record != null) {
            IWebResponseCallback getReportWebCallback = new IWebResponseCallback() {
                @Override
                public void onFinish(WebResponse response) {
                    Context context = RecordReportLayout.this.getContext();
                    if(response.getCode() == RETURN_CODE_SUCCESS) {
                        JSONObject reportResult = (JSONObject) response.getContent();
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
}
