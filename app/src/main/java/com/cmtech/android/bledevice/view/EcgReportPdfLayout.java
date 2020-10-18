package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledeviceapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.cmtech.android.bledevice.report.EcgReport.DONE;
import static com.cmtech.android.bledevice.report.EcgReport.INVALID_TIME;
import static com.cmtech.android.bledevice.report.EcgReport.PROCESS;
import static com.cmtech.android.bledevice.report.EcgReport.REQUEST;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.view
 * ClassName:      EcgReportPdfLayout
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/10/18 下午3:04
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/18 下午3:04
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class EcgReportPdfLayout extends LinearLayout {
    private BleEcgRecord10 record;

    private final TextView tvTime;
    private final TextView tvContent;
    private final TextView tvStatus;
    private final TextView tvAveHr;
    private ScanEcgView ecgView; // ecgView

    public EcgReportPdfLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_ecg_report_pdf, this);

        tvContent = view.findViewById(R.id.tv_report_content);
        tvTime = view.findViewById(R.id.tv_report_time);
        tvStatus = view.findViewById(R.id.tv_report_status);
        tvAveHr = view.findViewById(R.id.tv_report_ave_hr);
        ecgView = findViewById(R.id.roll_ecg_view);

    }

    public void setRecord(BleEcgRecord10 record) {
        this.record = record;
        updateView();
    }

    private void updateView() {
        if(record == null || record.getReport() == null) return;

        ecgView.setup(record.getSampleRate(), record.getCaliValue(), RollWaveView.DEFAULT_ZERO_LOCATION);
        ecgView.start();
        ecgView.initialize();
        List<Short> ecgData = record.getEcgData();
        for(Short d : ecgData) {
            ecgView.showData(d);
        }

        long time = record.getReport().getReportTime();
        if(time > INVALID_TIME) {
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

            tvContent.setText(record.getReport().getContent());

            tvAveHr.setText("平均心率为：" + record.getReport().getAveHr() + "bpm");
        } else {

        }
    }
}
