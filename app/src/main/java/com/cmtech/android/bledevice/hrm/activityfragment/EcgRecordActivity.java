package com.cmtech.android.bledevice.hrm.activityfragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord10;
import com.cmtech.android.bledeviceapp.interfac.ISimpleCallback;
import com.cmtech.android.bledeviceapp.view.layout.EcgReportOutputLayout;
import com.cmtech.android.bledeviceapp.view.OnRollWaveViewListener;
import com.cmtech.android.bledeviceapp.view.layout.RecordIntroductionLayout;
import com.cmtech.android.bledeviceapp.view.layout.RecordNoteLayout;
import com.cmtech.android.bledeviceapp.view.layout.RecordReportLayout;
import com.cmtech.android.bledeviceapp.view.RollEcgView;
import com.cmtech.android.bledeviceapp.view.RollWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;
import com.vise.utils.view.BitmapUtil;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.INVALID_ID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_CACHE;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class EcgRecordActivity extends AppCompatActivity implements OnRollWaveViewListener {
    private BleEcgRecord10 record; // record
    private RecordIntroductionLayout introductionLayout; // record introduction layout
    private RecordReportLayout reportLayout; // record report layout
    private RecordNoteLayout noteLayout; // record note layout
    private EcgReportOutputLayout reportOutputLayout; // record report output layout

    private RollEcgView ecgView; // ecgView
    private TextView tvTimeLength; // record time length
    private TextView tvCurrentTime; // current replay time
    private SeekBar sbReplay; // ecg replay seek bar
    private ImageButton ibReplayCtrl; // ecg replay control button


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ecg);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);
        record = LitePal.find(BleEcgRecord10.class, recordId, true);

        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        if(record.noSignal()) {
            record.download(this, code -> {
                if (code == RETURN_CODE_SUCCESS) {
                    initUI();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        } else {
            initUI();
        }
    }

    private void initUI() {
        introductionLayout = findViewById(R.id.layout_record_intro);
        introductionLayout.setRecord(record);
        introductionLayout.updateView();

        reportLayout = findViewById(R.id.layout_record_report);
        reportLayout.setRecord(record);
        reportLayout.updateView();

        noteLayout = findViewById(R.id.layout_record_note);
        noteLayout.setRecord(record);
        noteLayout.updateView();

        reportOutputLayout = findViewById(R.id.layout_ecg_report_output);
        reportOutputLayout.setRecord(record);

        ecgView = findViewById(R.id.roll_ecg_view);
        ecgView.setListener(this);
        ecgView.setup(record, RollWaveView.DEFAULT_ZERO_LOCATION);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvCurrentTime.setText(DateTimeUtil.secToMinute(0));

        tvTimeLength = findViewById(R.id.tv_time_length);
        int timeLength = record.getRecordSecond();
        tvTimeLength.setText(DateTimeUtil.secToMinute(timeLength));

        sbReplay = findViewById(R.id.sb_replay);
        sbReplay.setMax(timeLength);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    ecgView.showAtSecond(i);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        sbReplay.setEnabled(false);

        ibReplayCtrl = findViewById(R.id.ib_replay_control);
        ibReplayCtrl.setOnClickListener(view -> {
            if(ecgView.isShowing()) {
                ecgView.stopShow();
            } else {
                ecgView.startShow();
            }
        });

        TextView tvOutputReport = findViewById(R.id.tv_output_report);
        tvOutputReport.setOnClickListener(view -> {
            if(ecgView.isShowing())
                ecgView.stopShow();
            outputPdf();
        });

        ecgView.startShow();
    }

    private void outputPdf() {
        reportOutputLayout.outputPdf("将报告保存为PDF文件，请稍等...", () -> {
            PdfDocument doc = new PdfDocument();
            PdfDocument.PageInfo pageInfo =new PdfDocument.PageInfo.Builder(
                    reportOutputLayout.getWidth(), reportOutputLayout.getHeight(), 1)
                    .create();

            PdfDocument.Page page = doc.startPage(pageInfo);

            reportOutputLayout.draw(page.getCanvas());
            doc.finishPage(page);

            DateFormat dateFmt = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
            String docTime = dateFmt.format(new Date());
            //File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            try {
                File pdfFile = new File(DIR_CACHE,"km_ecgreport_"+docTime+".pdf");
                doc.writeTo(new FileOutputStream(pdfFile));
                doc.close();
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(EcgRecordActivity.this,
                            getApplicationContext().getPackageName() + ".provider", pdfFile);
                } else {
                    uri = Uri.fromFile(pdfFile);
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "application/pdf");
                startActivity(intent);
            } catch (IOException e) {
                Toast.makeText(EcgRecordActivity.this, "输出PDF文件失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    private void outputPng() {
        reportOutputLayout.outputPdf("将报告输出为PNG图片，请稍等...", () -> {
            PdfDocument doc = new PdfDocument();
            PdfDocument.PageInfo pageInfo =new PdfDocument.PageInfo.Builder(
                    reportOutputLayout.getWidth(), reportOutputLayout.getHeight(), 1)
                    .create();
            PdfDocument.Page page = doc.startPage(pageInfo);
            reportOutputLayout.draw(page.getCanvas());
            doc.finishPage(page);
            try {
                File pdfFile = new File(DIR_CACHE,"tmp.pdf");
                doc.writeTo(new FileOutputStream(pdfFile));
                doc.close();
                Bitmap bitmap = pdfToBitmap(pdfFile);
                DateFormat dateFmt = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
                String docTime = dateFmt.format(new Date());
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File pngFile = new File(dir, "km_ecgreport_"+docTime+".png");
                BitmapUtil.saveBitmap(bitmap, pngFile);
                Toast.makeText(EcgRecordActivity.this, "PNG图片已放入相册中。", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(EcgRecordActivity.this, "生成图片文件失败", Toast.LENGTH_SHORT).show();
                ViseLog.e(e);
                e.printStackTrace();
            }
        });
    }

    private  Bitmap pdfToBitmap(File pdfFile) {
        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
            PdfRenderer.Page page = renderer.openPage(0);

            int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
            int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
            int scale = 1;
            while(width > 2000) {
                scale *= 2;
                width /= 2;
            }
            height /= scale;
            //ViseLog.e("w="+width+"h="+height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            renderer.close();
            return bitmap;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDataLocationUpdated(long location, int second) {
        tvCurrentTime.setText(DateTimeUtil.secToMinute(second));
        sbReplay.setProgress(second);
    }

    @Override
    public void onShowStateUpdated(boolean show) {
        sbReplay.setEnabled(!show);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        if(ecgView != null)
            ecgView.stopShow();

        super.onDestroy();
    }
}
