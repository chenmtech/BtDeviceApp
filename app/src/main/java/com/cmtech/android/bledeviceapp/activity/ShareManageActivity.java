package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.RecordShareInfo;
import com.cmtech.android.bledeviceapp.util.KeyBoardUtil;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;

import java.util.List;

public class ShareManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_manage);
    }

    // 更新分享信息列表
    private void updateShareInfoList() {
        KeyBoardUtil.closeKeybord(this);

        // 从服务器下载满足条件的记录保存到本地数据库，之后再从本地数据库中读取满足条件的记录
        Account.downloadShareInfo(this, "请稍后...", new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if (code != RETURN_CODE_SUCCESS) {
                    Toast.makeText(ShareManageActivity.this, WebFailureHandler.toString(code), Toast.LENGTH_SHORT).show();
                }

                // 从本地读取记录
                List<RecordShareInfo> shareInfos = Account.readShareInfoFromLocalDb();

                if (shareInfos == null) {
                    Toast.makeText(ShareManageActivity.this, R.string.no_more, Toast.LENGTH_SHORT).show();
                } else {
                    updateView();
                }
            }
        });
    }

    private void updateView() {

    }
}