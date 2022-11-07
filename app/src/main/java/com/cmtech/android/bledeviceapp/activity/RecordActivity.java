package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
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
            public void onFinish(int code, String msg) {
                Toast.makeText(RecordActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 分享记录
     */
    public void shareRecord() {
        noteLayout.saveNote();
        if(record.needUpload()) {
            Toast.makeText(RecordActivity.this, "记录需要先上传到服务器，才能分享", Toast.LENGTH_SHORT).show();
            return;
        }

        if(record.getAccountId() != record.getCreatorId()) {
            Toast.makeText(this, "本条记录不能分享。", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(RecordActivity.this, ChooseSharePersonActivity.class);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                int contactPersonId = data.getIntExtra("contactPersonId", INVALID_ID);
                if(contactPersonId != INVALID_ID)
                    shareTo(contactPersonId);
            }
        }
    }

    private void shareTo(int toId) {
        if(MyApplication.getAccount().getCanShareToIdList().contains(toId)) {
            record.share(this, toId, new ICodeCallback() {
                @Override
                public void onFinish(int code, String msg) {
                    Toast.makeText(RecordActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(RecordActivity.this, "不能分享", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 下载记录
     */
    public void downloadRecord() {
        String reportVer = record.getReportVer();
        record.download(this, "下载记录中，请稍等。", (code, msg) -> {
            if (code == RCODE_SUCCESS) {
                if (record.getReportVer().compareTo(reportVer) > 0) {
                    Toast.makeText(this, "诊断报告已更新。", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                initUI();
            } else {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(noteLayout != null)
            noteLayout.saveNote();
        setResult(RESULT_OK);
        finish();
    }
}