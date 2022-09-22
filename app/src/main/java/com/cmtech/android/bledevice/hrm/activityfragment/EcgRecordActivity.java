package com.cmtech.android.bledevice.hrm.activityfragment;

import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_CACHE;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordActivity;
import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.view.OnRollWaveViewListener;
import com.cmtech.android.bledeviceapp.view.RollEcgView;
import com.cmtech.android.bledeviceapp.view.RollWaveView;
import com.cmtech.android.bledeviceapp.view.layout.EcgRecordReportLayout;
import com.cmtech.android.bledeviceapp.view.layout.EcgReportOutputLayout;
import com.vise.log.ViseLog;
import com.vise.utils.view.BitmapUtil;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 心电记录Activity
 */
public class EcgRecordActivity extends RecordActivity implements OnRollWaveViewListener {
    // 心电记录报告Layout
    private EcgRecordReportLayout reportLayout;

    // 心电记录报告输出layout，用于输出PDF
    private EcgReportOutputLayout reportOutputLayout;

    // 心电信号View
    private RollEcgView ecgView;

    // 记录时长
    private TextView tvTimeLength;

    // 当前回放时刻点
    private TextView tvCurrentTime;

    // 当前回放长时刻点
    private TextView tvCurrentLongTime;

    // 回放条
    private SeekBar sbReplay;

    // 回放开始/停止按钮
    private ImageButton ibReplayCtrl;

    // 输出PDF按钮
    private Button btnOutputPdf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ecg);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);

        LitePal.findAsync(BleEcgRecord.class, recordId, true).listen(new FindCallback<BleEcgRecord>() {
            @Override
            public void onFinish(BleEcgRecord bleEcgRecord) {
                bleEcgRecord.openSigFile();
                if(bleEcgRecord.noSignal()) {
                    bleEcgRecord.download(EcgRecordActivity.this, code -> {
                        if (code == RETURN_CODE_SUCCESS) {
                            bleEcgRecord.openSigFile();
                            record = bleEcgRecord;
                            initUI();
                        } else {
                            Toast.makeText(EcgRecordActivity.this, "记录已损坏。", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    record = bleEcgRecord;
                    initUI();
                }
            }
        });
    }

    @Override
    public void initUI() {
        super.initUI();

        ViseLog.e(record);

        reportLayout = findViewById(R.id.layout_record_report);
        reportLayout.setRecord((BleEcgRecord) record);
        reportLayout.updateView();

        reportOutputLayout = findViewById(R.id.layout_ecg_report_output);
        reportOutputLayout.setRecord((BleEcgRecord) record);

        ecgView = findViewById(R.id.roll_ecg_view);
        ecgView.setListener(this);
        ecgView.setup((BleEcgRecord) record, RollWaveView.DEFAULT_ZERO_LOCATION);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvCurrentTime.setText(DateTimeUtil.secToTime(0));

        tvCurrentLongTime = findViewById(R.id.tv_current_long_time);
        tvCurrentLongTime.setText(DateTimeUtil.timeToStringWithTodayYesterday(((BleEcgRecord)record).getCurrentPosTime()));

        tvTimeLength = findViewById(R.id.tv_time_length);
        int timeLength = record.getRecordSecond();
        tvTimeLength.setText(DateTimeUtil.secToTime(timeLength));

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

        btnOutputPdf = findViewById(R.id.btn_output_pdf);
        btnOutputPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ecgView.isShowing())
                    ecgView.stopShow();
                outputPdf();
            }
        });

        ecgView.startShow();
    }

    /*
    @Override
    public void uploadRecord() {
        //ViseLog.e("上传记录信号");
        assert DIR_DOC != null;
        File sigFile = FileUtil.getFile(DIR_DOC, ((BleEcgRecord)record).getSigFileName());
        boolean success = UploadDownloadFileUtil.uploadFile(this, "ECG", sigFile);
        if(success) {
            super.uploadRecord();
        } else {
            Toast.makeText(this, "上传失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void downloadRecord() {
        boolean success = UploadDownloadFileUtil.downloadFile(this, "ECG",
                ((BleEcgRecord)record).getSigFileName(), DIR_DOC);
        if(success) {
            super.downloadRecord();
        } else {
            Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
        }
    }


    private void showPopupMenu(View view) {
        // View当前PopupMenu显示的相对View的位置
        PopupMenu popupMenu = new PopupMenu(this, view);
        // menu布局
        popupMenu.getMenuInflater().inflate(R.menu.menu_ecg_report, popupMenu.getMenu());
        // menu的item点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.local_diagnose:
                        reportLayout.localDiagnose();
                        break;
                    case R.id.remote_diagnose:
                        reportLayout.remoteDiagnose();
                        break;
                    case R.id.output_report:
                        if(ecgView.isShowing())
                            ecgView.stopShow();
                        outputPdf();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        popupMenu.show();
    }
    */

    /**
     * 将记录输出为PDF文档，文档结构按照reportOutputLayout
     */
    private void outputPdf() {
        reportOutputLayout.output("将报告输出为PDF文件，请稍等...", () -> {
            PdfDocument doc = new PdfDocument();

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    reportOutputLayout.getWidth(), reportOutputLayout.getHeight(), 1)
                    .create();

            PdfDocument.Page page = doc.startPage(pageInfo);

            reportOutputLayout.draw(page.getCanvas());

            doc.finishPage(page);

            String docTime = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(new Date());
            //File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            try {
                // 将记录写入临时文件中
                File pdfFile = new File(DIR_CACHE,"km_ecgreport_"+docTime+".pdf");
                doc.writeTo(new FileOutputStream(pdfFile));
                doc.close();

                // 启动app打开PDF文档
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

    /**
     * 将记录输出为PNG图片，按照reportOutputLayout
     */
    private void outputPng() {
        reportOutputLayout.output("将报告输出为PNG图片，请稍等...", () -> {
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

    /**
     * 将PDF文档转换为Bitmap
     * @param pdfFile
     * @return
     */
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
        tvCurrentTime.setText(DateTimeUtil.secToTime(second));
        tvCurrentLongTime.setText(DateTimeUtil.timeToStringWithTodayYesterday(((BleEcgRecord)record).getCurrentPosTime()));
        sbReplay.setProgress(second);
    }

    @Override
    public void onShowStateUpdated(boolean show) {
        sbReplay.setEnabled(!show);
    }

    @Override
    public void onBackPressed() {
        if(ecgView != null)
            ecgView.stopShow();

        if(record != null)
            ((BleEcgRecord)record).closeSigFile();

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
