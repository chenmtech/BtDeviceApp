package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.DONE;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;
import com.cmtech.android.bledeviceapp.view.layout.RecordIntroductionLayout;
import com.cmtech.android.bledeviceapp.view.layout.RecordNoteLayout;

public abstract class RecordActivity extends AppCompatActivity {
    protected BasicRecord record; // record
    protected RecordIntroductionLayout introLayout;
    protected RecordNoteLayout noteLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initUI() {
        introLayout = findViewById(R.id.layout_record_intro);
        introLayout.setRecord(record);
        introLayout.updateView();

        noteLayout = findViewById(R.id.layout_record_note);
        noteLayout.setRecord(record);
        noteLayout.updateView();
    }

    /**
     * 上传记录
     */
    public void uploadRecord() {
        noteLayout.saveNote();
        record.upload(this, new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if (code == RETURN_CODE_SUCCESS) {
                    Toast.makeText(RecordActivity.this, "记录已上传", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RecordActivity.this, WebFailureHandler.toString(code), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 下载记录
     */
    public void downloadRecord() {
        String reportVer = record.getReportVer();
        record.download(this, code -> {
            if (code == RETURN_CODE_SUCCESS) {
                if (record.getReportVer().compareTo(reportVer) > 0) {
                    Toast.makeText(this, "诊断报告已更新。", Toast.LENGTH_SHORT).show();
                }
                initUI();
            } else {
                Toast.makeText(this, "下载记录失败。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        noteLayout.saveNote();
        setResult(RESULT_OK);
        finish();
    }
}