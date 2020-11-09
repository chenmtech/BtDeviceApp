package com.cmtech.android.bledeviceapp.view.layout;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.interfac.ISimpleCallback;
import com.cmtech.android.bledeviceapp.view.RollWaveView;
import com.cmtech.android.bledeviceapp.view.ScanEcgView;
import com.cmtech.android.bledeviceapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.cmtech.android.bledeviceapp.data.report.EcgReport.INVALID_TIME;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.view
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
    private static final ScanEcgView[] ECG_VIEWS = new ScanEcgView[3]; // ecgView array

    private BleEcgRecord record;
    private final TextView tvRecordPerson;
    private final TextView tvRecordTime;
    private final TextView tvReportVer;
    private final TextView tvReportTime;
    private final TextView tvReportPrintTime;
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
        tvReportPrintTime = view.findViewById(R.id.tv_report_print_time);
        tvNote = view.findViewById(R.id.tv_note);
        tvAveHr= view.findViewById(R.id.tv_report_ave_hr);
        ECG_VIEWS[0] = view.findViewById(R.id.roll_ecg_view1);
        ECG_VIEWS[1] = view.findViewById(R.id.roll_ecg_view2);
        ECG_VIEWS[2] = view.findViewById(R.id.roll_ecg_view3);
    }

    public void setRecord(BleEcgRecord record) {
        this.record = record;
    }

    public void outputPdf(String showText, ISimpleCallback callback) {
        if(record == null) return;

        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        tvRecordPerson.setText(record.getCreatorNickName());
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

        tvReportPrintTime.setText(dateFmt.format(new Date().getTime()));
        tvNote.setText(record.getNote());

        new DrawEcgViewAsyncTask(getContext(), showText, callback).execute(record);
    }

    private static class DrawEcgViewAsyncTask extends AsyncTask<BleEcgRecord, Void, Void> {
        private final ProgressDialog progressDialog;
        private final ISimpleCallback callback;

        private DrawEcgViewAsyncTask(Context context, String showText, ISimpleCallback callback) {
            this.callback = callback;
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(showText);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(BleEcgRecord... bleEcgRecords) {
            BleEcgRecord record = bleEcgRecords[0];
            List<Short> ecgData = record.getEcgData();

            int begin = 0;
            for (ScanEcgView scanEcgView : ECG_VIEWS) {
                scanEcgView.setup(record.getSampleRate(), record.getCaliValue(), RollWaveView.DEFAULT_ZERO_LOCATION);
                int dataNum = scanEcgView.getWidth() / scanEcgView.getPixelPerData();
                if (ecgData.size() > begin) {
                    scanEcgView.startShow();
                    int end = Math.min(begin + dataNum, ecgData.size());
                    for (int j = begin; j < end; j++) {
                        scanEcgView.addData(ecgData.get(j));
                    }
                    scanEcgView.stopShow();
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
