package com.cmtech.android.bledeviceapp.view.layout;

import static com.cmtech.android.bledeviceapp.data.report.EcgReport.DONE;
import static com.cmtech.android.bledeviceapp.data.report.EcgReport.INVALID_TIME;
import static com.cmtech.android.bledeviceapp.data.report.EcgReport.LOCAL;
import static com.cmtech.android.bledeviceapp.data.report.EcgReport.PROCESS;
import static com.cmtech.android.bledeviceapp.data.report.EcgReport.REQUEST;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EcgRecordReportLayout extends LinearLayout {
    private BleEcgRecord record;

    private final EditText etAveHr;
    private final TextView tvReportClient;
    private final EditText etContent;
    private final TextView tvTime;
    private final TextView tvReportVer;

    public EcgRecordReportLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_report, this);

        etAveHr = view.findViewById(R.id.et_report_ave_hr);
        tvReportClient = view.findViewById(R.id.tv_report_client);
        etContent = view.findViewById(R.id.et_report_content);
        tvTime = view.findViewById(R.id.tv_report_time);
        tvReportVer = view.findViewById(R.id.tv_report_ver);

    }

    public void setRecord(BleEcgRecord record) {
        this.record = record;
    }

    public void localDiagnose() {
        if(record != null) {
            /*if(MyEcgArrhythmiaDetector.VER.compareTo(record.getReport().getVer()) > 0) {
                record.requestDiagnose();
                updateView();
                Toast.makeText(getContext(), "已更新检测结果", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "当前报告是最新版本。", Toast.LENGTH_SHORT).show();
            }*/
            record.localDiagnose();
            updateView();
            Toast.makeText(getContext(), "已更新检测结果", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateView() {
        if(record == null || record.getReport() == null) return;

        long time = record.getReport().getReportTime();
        if(time > INVALID_TIME) {
            DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            tvTime.setText(dateFmt.format(time));
            etAveHr.setText(String.valueOf(record.getReport().getAveHr()));
            if(record.getReport().getReportClient() == LOCAL)
                tvReportClient.setText("本地检测：");
            else
                tvReportClient.setText("远程检测：");
            etContent.setText(record.getReport().getContent());
            tvReportVer.setText(record.getReport().getVer());
        } else {

        }
    }

    public void remoteDiagnose() {
        if(record != null) {
            IWebResponseCallback getReportWebCallback = new IWebResponseCallback() {
                @Override
                public void onFinish(WebResponse response) {
                    Context context = EcgRecordReportLayout.this.getContext();
                    if(response.getCode() == RETURN_CODE_SUCCESS) {
                        JSONObject reportResult = (JSONObject) response.getContent();
                        try {
                            EcgReport report = new EcgReport();
                            report.fromJson(reportResult);
                            ViseLog.e(report);
                            int status = report.getStatus();
                            switch (status) {
                                case DONE:
                                    record.getReport().setVer(report.getVer());
                                    record.getReport().setReportClient(report.getReportClient());
                                    record.getReport().setReportTime(report.getReportTime());
                                    record.getReport().setContent(report.getContent());
                                    record.getReport().setStatus(report.getStatus());
                                    record.getReport().setAveHr(report.getAveHr());
                                    record.getReport().save();
                                    //setNeedUpload(true);
                                    record.save();
                                    updateView();
                                    Toast.makeText(context, "检测报告已更新", Toast.LENGTH_SHORT).show();
                                    break;

                                case REQUEST:
                                    Toast.makeText(context, "已申请检测，请稍后更新", Toast.LENGTH_SHORT).show();
                                    break;

                                case PROCESS:
                                    Toast.makeText(context, "正在检测，请稍后更新", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context, "获取检测报告错误", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            record.remoteDiagnose(getContext(), getReportWebCallback);
        }
    }
}
