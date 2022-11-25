package com.cmtech.android.bledeviceapp.view.layout;

import static com.cmtech.android.bledeviceapp.util.DateTimeUtil.INVALID_TIME;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.interfac.ISimpleCallback;
import com.cmtech.android.bledeviceapp.view.ScanEcgView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 心电记录报告输出Layout
 */
public class EcgReportOutputLayout extends LinearLayout {
    // 关联的记录
    private BleEcgRecord record;

    // 用户显示心电信号的View数组
    private static final ScanEcgView[] ECG_VIEWS = new ScanEcgView[3]; // ecgView array

    // 记录人名
    private final TextView tvRecordPerson;

    // 记录人备注
    private final TextView tvRecordPersonNote;

    // 记录起始时间
    private final TextView tvRecordBeginTime;

    // X方向分辨率
    private final TextView tvXResolution;

    // Y方向分辨率
    private final TextView tvYResolution;

    // 报告版本
    private final TextView tvReportVer;

    // 报告提供方
    private final TextView tvReportProvider;

    // 报告产生时间
    private final TextView tvReportTime;

    // 报告内容
    private final TextView tvReportContent;

    // 备注
    private final TextView tvNote;

    // 打印时间
    private final TextView tvPrintTime;

    public EcgReportOutputLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_ecg_report_output, this);

        tvRecordPerson = view.findViewById(R.id.tv_record_person);
        tvRecordPersonNote = view.findViewById(R.id.tv_record_person_note);
        tvRecordBeginTime = view.findViewById(R.id.tv_record_begin_time);
        tvXResolution = view.findViewById(R.id.tv_x_resolution);
        tvYResolution = view.findViewById(R.id.tv_y_resolution);
        tvReportContent = view.findViewById(R.id.tv_report_content);
        tvReportVer = view.findViewById(R.id.tv_report_ver);
        tvReportProvider = view.findViewById(R.id.tv_report_provider);
        tvReportTime = view.findViewById(R.id.tv_report_time);
        tvPrintTime = view.findViewById(R.id.tv_report_print_time);
        tvNote = view.findViewById(R.id.tv_note);
        ECG_VIEWS[0] = view.findViewById(R.id.roll_ecg_view1);
        ECG_VIEWS[1] = view.findViewById(R.id.roll_ecg_view2);
        ECG_VIEWS[2] = view.findViewById(R.id.roll_ecg_view3);
    }

    /**
     * 设置关联记录
     * @param record
     */
    public void setRecord(BleEcgRecord record) {
        this.record = record;
    }

    /**
     * 将该Layout内容输出
     * 输出过程分为两步：第一步，先将要显示的内容设置好；第二步，调用callback将内容输出
     * @param showText：输出过程中要显示的字符串
     * @param callback：将内容输出的回调
     */
    public void output(String showText, ISimpleCallback callback) {
        if(record == null) return;

        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        tvRecordPerson.setText(record.getCreatorNickNameInOutputReport());
        tvRecordPersonNote.setText(record.getCreatorNoteInOutputReport());
        long endTime = record.getCreateTime();
        long beginTime = endTime-record.getSigLen()* 1000L /record.getSampleRate();
        tvRecordBeginTime.setText(dateFmt.format(beginTime));

        tvXResolution.setText(String.format(Locale.getDefault(), "%.1f", ScanEcgView.SECOND_PER_GRID*5));
        tvYResolution.setText(String.format(Locale.getDefault(), "%.1f", ScanEcgView.MV_PER_GRID*5));

        long reportTime = record.getReportTime();
        DateFormat dateFmt1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        if(reportTime > INVALID_TIME) {
            tvReportVer.setText(record.getReportVer());
            tvReportTime.setText(dateFmt1.format(reportTime));
            tvReportContent.setText(record.getReportContent());
            tvReportProvider.setText(record.getReportProvider());
        }

        tvPrintTime.setText(dateFmt1.format(new Date().getTime()));
        tvNote.setText(record.getComment());

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
            record.seek(0);

            int begin = 0;
            for (ScanEcgView scanEcgView : ECG_VIEWS) {
                scanEcgView.setup(record.getSampleRate(), record.getGain().get(0));
                int dataNum = scanEcgView.getWidth() / scanEcgView.getPixelPerData();
                if (record.getDataNum() > begin) {
                    scanEcgView.startShow();
                    int end = Math.min(begin + dataNum, record.getDataNum());
                    for (int j = begin; j < end; j++) {
                        try {
                            scanEcgView.addData(record.readData());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
