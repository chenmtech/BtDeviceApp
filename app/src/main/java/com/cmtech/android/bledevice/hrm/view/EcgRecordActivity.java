package com.cmtech.android.bledevice.hrm.view;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledevice.view.EcgReportPdfLayout;
import com.cmtech.android.bledevice.view.RecordIntroductionLayout;
import com.cmtech.android.bledevice.view.RecordNoteLayout;
import com.cmtech.android.bledevice.view.RecordReportLayout;
import com.cmtech.android.bledevice.view.RollEcgRecordWaveView;
import com.cmtech.android.bledevice.view.RollWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.cmtech.android.bledevice.record.BasicRecord.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class EcgRecordActivity extends AppCompatActivity implements RollWaveView.OnRollWaveViewListener{
    private BleEcgRecord10 record; // record
    private RecordIntroductionLayout introductionLayout; // record introduction layout
    private RecordReportLayout reportLayout; // record report layout
    private RecordNoteLayout noteLayout; // record note layout
    private EcgReportPdfLayout reportPdfLayout; // record report PDF layout

    private RollEcgRecordWaveView ecgView; // ecgView
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

        reportPdfLayout = findViewById(R.id.layout_ecg_report_pdf);
        reportPdfLayout.setRecord(record);

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
                if(ecgView.isStart()) {
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

        // set output pdf FAB
        FloatingActionButton fabPdf = findViewById(R.id.fab_pdf);
        fabPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ecgView.isStart())
                    ecgView.stopShow();
                outputPdf();
            }
        });

        ecgView.startShow();
    }

    private void outputPdf() {
        reportPdfLayout.updateView(new EcgReportPdfLayout.IPdfOutputCallback() {
            @Override
            public void onFinish() {
                PdfDocument doc = new PdfDocument();
                PdfDocument.PageInfo pageInfo =new PdfDocument.PageInfo.Builder(
                        reportPdfLayout.getWidth(), reportPdfLayout.getHeight(), 1)
                        .create();

                PdfDocument.Page page = doc.startPage(pageInfo);

                reportPdfLayout.draw(page.getCanvas());
                doc.finishPage(page);

                DateFormat dateFmt = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
                String docTime = dateFmt.format(new Date());
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                try {
                    if(!dir.exists()) {
                        dir.mkdir();
                    }
                    File pdfFile = new File(dir,"km_ecgreport_" + docTime + ".pdf");
                    doc.writeTo(new FileOutputStream(pdfFile));
                    doc.close();

                    final AlertDialog.Builder builder = new AlertDialog.Builder(EcgRecordActivity.this);
                    builder.setTitle("已生成pdf文档").setMessage("是否打开该文档？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);

                            Uri uri = FileProvider.getUriForFile(EcgRecordActivity.this,
                                    getApplicationContext().getPackageName() + ".provider", new File(dir,"km_ecgreport_" + docTime + ".pdf"));
                            //Uri uri = Uri.fromFile(pdfFile);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(uri, "application/pdf");
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                ViseLog.e("打开pdf文档失败。");
                            }
                        }
                    }).show();
                } catch (IOException e) {
                    Toast.makeText(EcgRecordActivity.this, "生成PDF文件失败", Toast.LENGTH_SHORT).show();
                    ViseLog.e(e);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDataLocationUpdated(long dataLocation, int sampleRate) {
        int second = (int)(dataLocation/ sampleRate);
        tvCurrentTime.setText(DateTimeUtil.secToMinute(second));
        sbReplay.setProgress(second);
    }

    @Override
    public void onShowStateUpdated(boolean isShow) {
        sbReplay.setEnabled(!isShow);
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
