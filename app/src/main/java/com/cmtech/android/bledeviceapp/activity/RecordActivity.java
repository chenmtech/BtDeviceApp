package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.cmtech.android.bledevice.hrm.activityfragment.EcgRecordActivity;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;

public class RecordActivity extends AppCompatActivity {
    protected BasicRecord record; // record

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // 上传记录
    public void uploadRecord() {
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

    public void downloadRecord() {
        String reportVer = record.getReportVer();
        record.download(this, code -> {
            if (code == RETURN_CODE_SUCCESS) {
                onCreate(null);
                if (!record.getReportVer().equals(reportVer)) {
                    Toast.makeText(this, "诊断报告已更新。", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "无法下载记录，请检查网络是否正常。", Toast.LENGTH_SHORT).show();
            }
        });
    }
}