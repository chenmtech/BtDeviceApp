package com.cmtech.android.bledevice.hrm.view;

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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledevice.view.EcgReportOutputLayout;
import com.cmtech.android.bledevice.view.OnRollWaveViewListener;
import com.cmtech.android.bledevice.view.RecordIntroductionLayout;
import com.cmtech.android.bledevice.view.RecordNoteLayout;
import com.cmtech.android.bledevice.view.RecordReportLayout;
import com.cmtech.android.bledevice.view.RollEcgView;
import com.cmtech.android.bledevice.view.RollWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
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

import static com.cmtech.android.bledevice.record.BasicRecord.INVALID_ID;
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
    private SeekBar sbReplay; // 播放条
    private ImageButton ibReplayCtrl; // 播放状态控制


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ecg);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);
        record = LitePal.find(BleEcgRecord10.class, recordId, true);

        if(record == null) {
            Toast.makeText(EcgRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        ViseLog.e(record);

        if(record.noSignal()) {
            record.download(this, new IWebCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    if (code == RETURN_CODE_SUCCESS) {
                        initUI();
                    } else {
                        Toast.makeText(EcgRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
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
        int second = record.getRecordSecond();
        tvTimeLength.setText(DateTimeUtil.secToMinute(second));

        ibReplayCtrl = findViewById(R.id.ib_replay_control);
        ibReplayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ecgView.isShowing()) {
                    ecgView.stopShow();
                } else {
                    ecgView.startShow();
                }
            }
        });

        sbReplay = findViewById(R.id.sb_replay);
        sbReplay.setEnabled(false);
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
        sbReplay.setMax(second);

        // set output report FAB
        FloatingActionButton fabOutputReport = findViewById(R.id.fab_output_report);
        fabOutputReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ecgView.isShowing())
                    ecgView.stopShow();
                outputPdf();
            }
        });
        ecgView.startShow();
    }

    private void outputPng() {
        reportOutputLayout.updateView("将报告输出为PNG图片，请稍等...", new EcgReportOutputLayout.IPdfOutputCallback() {
            @Override
            public void onFinish() {
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
            }
        });
    }

    private void outputPdf() {
        reportOutputLayout.updateView("将报告保存为PDF文件，请稍等...", new EcgReportOutputLayout.IPdfOutputCallback() {
            @Override
            public void onFinish() {
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
                File dir = DIR_CACHE;
                try {
                    File pdfFile = new File(dir,"km_ecgreport_"+docTime+".pdf");
                    doc.writeTo(new FileOutputStream(pdfFile));
                    doc.close();
                    //Toast.makeText(EcgRecordActivity.this, "请到文件管理器中查阅PDF文件。", Toast.LENGTH_LONG).show();
                    Uri uri = null;
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
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        ViseLog.e("打开PDF文件错误。");
                    }
                } catch (IOException e) {
                    Toast.makeText(EcgRecordActivity.this, "保存PDF文件失败", Toast.LENGTH_SHORT).show();
                    ViseLog.e(e);
                    e.printStackTrace();
                }

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
        super.onDestroy();

        if(ecgView != null)
            ecgView.stopShow();
    }
}
