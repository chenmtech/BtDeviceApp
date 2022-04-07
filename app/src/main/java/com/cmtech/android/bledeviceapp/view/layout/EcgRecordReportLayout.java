package com.cmtech.android.bledeviceapp.view.layout;

import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.DONE;
import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.INVALID_TIME;
import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.LOCAL;
import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.PROCESS;
import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.REQUEST;
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

    private final TextView tvReportClient;
    private final EditText etContent;
    private final TextView tvTime;
    private final TextView tvReportVer;

    public EcgRecordReportLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_report, this);

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
            Toast.makeText(getContext(), "已更新诊断结果", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateView() {
        if(record == null) return;

        long time = record.getReportTime();
        if(time > INVALID_TIME) {
            DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            tvTime.setText(dateFmt.format(time));
            if(record.getReportClient() == LOCAL)
                tvReportClient.setText("本地诊断结果：");
            else
                tvReportClient.setText("远程诊断结果：");
            etContent.setText(record.getContent());
            tvReportVer.setText(record.getReportVer());
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
                                    record.setReportVer(report.getVer());
                                    record.setReportClient(report.getReportClient());
                                    record.setReportTime(report.getReportTime());
                                    record.setContent(report.getContent());
                                    record.setStatus(report.getStatus());
                                    //record.getReport().setAveHr(report.getAveHr());
                                    //record.getReport().save();
                                    //setNeedUpload(true);
                                    record.save();
                                    updateView();
                                    Toast.makeText(context, "诊断已更新", Toast.LENGTH_SHORT).show();
                                    break;

                                case REQUEST:
                                    Toast.makeText(context, "已申请诊断，请稍后更新", Toast.LENGTH_SHORT).show();
                                    break;

                                case PROCESS:
                                    Toast.makeText(context, "正在诊断中，请稍后更新", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context, "获取诊断报告失败", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            record.remoteDiagnose(getContext(), getReportWebCallback);
        }
    }
}
