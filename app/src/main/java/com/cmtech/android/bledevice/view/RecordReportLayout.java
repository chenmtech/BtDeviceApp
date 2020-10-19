package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

import static com.cmtech.android.bledevice.record.IDiagnosable.CODE_REPORT_FAILURE;
import static com.cmtech.android.bledevice.record.IDiagnosable.CODE_REPORT_NO_NEW;
import static com.cmtech.android.bledevice.record.IDiagnosable.CODE_REPORT_SUCCESS;
import static com.cmtech.android.bledevice.report.EcgReport.INVALID_TIME;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class RecordReportLayout extends LinearLayout {
    private BleEcgRecord10 record;

    private final TextView tvAveHr;
    private final TextView tvContent;
    private final TextView tvTime;

    public RecordReportLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_report, this);

        tvAveHr = view.findViewById(R.id.tv_report_ave_hr);
        tvContent = view.findViewById(R.id.tv_report_content);
        tvTime = view.findViewById(R.id.tv_report_time);

        Button btnRefresh = view.findViewById(R.id.btn_request_report);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(record != null) {
                    record.requestDiagnose();
                    updateView();
                }
            }
        });
    }

    public void setRecord(BleEcgRecord10 record) {
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
        } else {

        }
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
}
