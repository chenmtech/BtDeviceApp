package com.cmtech.android.bledevice.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
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
public class EcgReportOutputLayout extends LinearLayout {
    private BleEcgRecord10 record;

    private final ScanEcgView[] ecgView = new ScanEcgView[3]; // ecgView array
    private final TextView tvRecordPerson;
    private final TextView tvRecordTime;
    private final TextView tvReportVer;
    private final TextView tvReportTime;
    private final TextView tvContent;
    private final TextView tvAveHr;
    private final TextView tvNote;


    public EcgReportOutputLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_ecg_report_output, this);

        tvRecordPerson = view.findViewById(R.id.tv_record_person);
        tvRecordTime = view.findViewById(R.id.tv_record_time);
        tvContent = view.findViewById(R.id.tv_report_content);
        tvReportVer = view.findViewById(R.id.tv_report_ver);
        tvReportTime = view.findViewById(R.id.tv_report_time);
        tvNote = view.findViewById(R.id.tv_note);
        tvAveHr= view.findViewById(R.id.tv_report_ave_hr);
        ecgView[0] = view.findViewById(R.id.roll_ecg_view1);
        ecgView[1] = view.findViewById(R.id.roll_ecg_view2);
        ecgView[2] = view.findViewById(R.id.roll_ecg_view3);
    }

    public void setRecord(BleEcgRecord10 record) {
        this.record = record;
    }

    public void updateView(String showText, IPdfOutputCallback callback) {
        if(record == null) return;

        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        tvRecordPerson.setText(record.getCreatorName());
        long endTime = record.getCreateTime();
        long beginTime = endTime-record.getRecordSecond()*1000;
        tvRecordTime.setText(String.format("从%s 到%s", dateFmt.format(beginTime), dateFmt.format(endTime)));

        long reportTime = record.getReport().getReportTime();
        if(reportTime > INVALID_TIME) {
            tvReportVer.setText(record.getReport().getVer());
            tvReportTime.setText(dateFmt.format(reportTime));
            tvContent.setText(record.getReport().getContent());
            tvAveHr.setText(String.valueOf(record.getReport().getAveHr()));
        }

        tvNote.setText(record.getNote());

        new DrawEcgViewAsyncTask(getContext(), showText, callback).execute(record);
    }

    public interface IPdfOutputCallback {
        void onFinish();
    }

    private class DrawEcgViewAsyncTask extends AsyncTask<BleEcgRecord10, Void, Void> {
        private final ProgressDialog progressDialog;
        private final IPdfOutputCallback callback;

        private DrawEcgViewAsyncTask(Context context, String showText, IPdfOutputCallback callback) {
            this.callback = callback;
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(showText);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(BleEcgRecord10... bleEcgRecord10s) {
            record = bleEcgRecord10s[0];
            List<Short> ecgData = record.getEcgData();

            int begin = 0;
            for (ScanEcgView scanEcgView : ecgView) {
                scanEcgView.setup(record.getSampleRate(), record.getCaliValue(), RollWaveView.DEFAULT_ZERO_LOCATION);
                int dataNum = scanEcgView.getWidth() / scanEcgView.getxPixelPerData();
                if (ecgData.size() > begin) {
                    scanEcgView.start();
                    scanEcgView.reset();
                    int end = Math.min(begin + dataNum, ecgData.size());
                    for (int j = begin; j < end; j++) {
                        scanEcgView.showData(ecgData.get(j));
                    }
                    scanEcgView.stop();
                    begin = end;
                } else {
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(callback != null)
                callback.onFinish();
            progressDialog.dismiss();
        }
    }
}
