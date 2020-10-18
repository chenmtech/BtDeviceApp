package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledeviceapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.cmtech.android.bledevice.report.EcgReport.INVALID_TIME;

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

    private final ScanEcgView ecgView1; // ecgView
    private final ScanEcgView ecgView2; // ecgView
    private final ScanEcgView ecgView3; // ecgView
    private TextView tvTime;
    private TextView tvContent;
    private final TextView tvAveHr;
    private final TextView tvNote;

    public EcgReportPdfLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_ecg_report_pdf, this);

        tvContent = findViewById(R.id.tv_report_content);
        tvTime = findViewById(R.id.tv_report_time);
        tvNote = view.findViewById(R.id.tv_note);
        tvAveHr = view.findViewById(R.id.tv_report_ave_hr);
        ecgView1 = findViewById(R.id.roll_ecg_view1);
        ecgView2 = findViewById(R.id.roll_ecg_view2);
        ecgView3 = findViewById(R.id.roll_ecg_view3);

    }

    public void setRecord(BleEcgRecord10 record) {
        this.record = record;
        updateView();
    }

    private void updateView() {
        if(record == null || record.getReport() == null) return;

        ecgView1.setup(record.getSampleRate(), record.getCaliValue(), RollWaveView.DEFAULT_ZERO_LOCATION);
        ecgView1.start();
        ecgView1.initialize();
        List<Short> ecgData = record.getEcgData();
        int dataNum = ecgView1.getWidth()/ ecgView1.getxPixelPerData();
        for(int i = 0; i < dataNum; i++) {
            ecgView1.showData(ecgData.get(i));
        }
        ecgView1.stop();
        ecgView2.setup(record.getSampleRate(), record.getCaliValue(), RollWaveView.DEFAULT_ZERO_LOCATION);
        ecgView2.start();
        ecgView2.initialize();
        for(int i = dataNum; i < 2*dataNum; i++) {
            ecgView2.showData(ecgData.get(i));
        }
        ecgView2.stop();
        ecgView3.setup(record.getSampleRate(), record.getCaliValue(), RollWaveView.DEFAULT_ZERO_LOCATION);
        ecgView3.start();
        ecgView3.initialize();
        int minL = Math.min(3*dataNum, ecgData.size());
        for(int i = 2*dataNum; i < minL; i++) {
            ecgView3.showData(ecgData.get(i));
        }
        ecgView3.stop();

        long time = record.getReport().getReportTime();
        if(time > INVALID_TIME) {
            DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            tvTime.setText(dateFmt.format(time));
            tvContent.setText(record.getReport().getContent());

            tvAveHr.setText("平均心率为：" + record.getReport().getAveHr() + "bpm");

            tvNote.setText(record.getNote());
        } else {

        }
    }
}
