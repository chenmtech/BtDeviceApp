package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.global.MyApplication;
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
     * 分享记录
     */
    public void shareRecord() {
        noteLayout.saveNote();
        if(record.needUpload()) {
            Toast.makeText(RecordActivity.this, "记录需要先上传到服务器，才能分享", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText et = new EditText(this);
        new AlertDialog.Builder(this).setTitle("请输入对方ID号")
                .setIcon(R.mipmap.ic_share_32px)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String idStr = et.getText().toString();
                        try {
                            int id = Integer.parseInt(idStr);
                            shareTo(id);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "无效ID号",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("取消",null).show();

    }

    private void shareTo(int toId) {
        if(MyApplication.getAccount().canShareTo(toId)) {
            record.share(this, toId, new ICodeCallback() {
                @Override
                public void onFinish(int code) {
                    if (code == RETURN_CODE_SUCCESS) {
                        Toast.makeText(RecordActivity.this, "记录已分享", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RecordActivity.this, WebFailureHandler.toString(code), Toast.LENGTH_SHORT).show();
                    }
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
        record.download(this, code -> {
            if (code == RETURN_CODE_SUCCESS) {
                if (record.getReportVer().compareTo(reportVer) > 0) {
                    Toast.makeText(this, "诊断报告已更新。", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "记录已下载。", Toast.LENGTH_SHORT).show();
                }
                initUI();
            } else {
                Toast.makeText(this, "下载记录失败。", Toast.LENGTH_SHORT).show();
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