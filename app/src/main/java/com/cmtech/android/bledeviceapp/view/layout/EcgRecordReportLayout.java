package com.cmtech.android.bledeviceapp.view.layout;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_TIME;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 心电记录诊断报告Layout
 */
public class EcgRecordReportLayout extends LinearLayout {
    // 该Layout关联的记录
    private BleEcgRecord record;

    // 报告的内容
    private final EditText etReportContent;

    // 报告时间
    private final TextView tvReportTime;

    // 报告版本
    private final TextView tvReportVer;

    // 报告提供方
    private final TextView tvReportProvider;

    public EcgRecordReportLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_report, this);

        etReportContent = view.findViewById(R.id.et_report_content);
        tvReportTime = view.findViewById(R.id.tv_report_time);
        tvReportVer = view.findViewById(R.id.tv_report_ver);
        tvReportProvider = view.findViewById(R.id.tv_report_provider);
    }

    /**
     * 设置关联的记录
     * @param record
     */
    public void setRecord(BleEcgRecord record) {
        this.record = record;
    }

    /**
     * 更新显示
     */
    public void updateView() {
        if(record == null) return;

        long time = record.getReportTime();
        if(time > INVALID_TIME) {
            DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            tvReportTime.setText(dateFmt.format(time));
            etReportContent.setText(record.getReportContent());
            tvReportVer.setText(record.getReportVer());
            tvReportProvider.setText(record.getReportProvider());
        } else {

        }
    }

    /*
    public void localDiagnose() {
        if(record != null) {
            record.localDiagnose();
            updateView();
            Toast.makeText(getContext(), "已更新诊断结果", Toast.LENGTH_SHORT).show();
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
                            int status = report.getReportStatus();
                            switch (status) {
                                case DONE:
                                    if(record.getReportVer().compareTo(report.getVer()) >= 0) {
                                        Toast.makeText(context, "诊断无更新", Toast.LENGTH_SHORT).show();
                                    } else {
                                        record.setReportVer(report.getVer());
                                        record.setReportProvider(report.getReportProvider());
                                        record.setReportTime(report.getReportTime());
                                        record.setReportContent(report.getReportContent());
                                        record.setReportStatus(report.getReportStatus());
                                        record.setAveHr(report.getAveHr());
                                        //setNeedUpload(true);
                                        record.save();
                                        updateView();
                                        Toast.makeText(context, "诊断已更新", Toast.LENGTH_SHORT).show();
                                    }
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
                        Toast.makeText(context, "请先将记录上传，才能更新诊断。", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            record.remoteDiagnose(getContext(), getReportWebCallback);
        }
    }
    */
}
