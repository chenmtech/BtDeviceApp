package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.ShareInfoAdapter;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.KeyBoardUtil;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;

public class ShareManageActivity extends AppCompatActivity {

    private ShareInfoAdapter shareInfoAdapter;
    private RecyclerView rvShareInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_manage);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_share_manage);
        setSupportActionBar(toolbar);

        // 初始化扫描设备列表
        rvShareInfo = findViewById(R.id.rv_share_info);
        rvShareInfo.setLayoutManager(new LinearLayoutManager(this));
        rvShareInfo.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        shareInfoAdapter = new ShareInfoAdapter(MyApplication.getShareInfoList());
        rvShareInfo.setAdapter(shareInfoAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;

            case R.id.item_refresh:
                updateShareInfoList();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 更新分享信息列表
    private void updateShareInfoList() {
        KeyBoardUtil.closeKeybord(this);

        // 从服务器下载满足条件的记录保存到本地数据库，之后再从本地数据库中读取满足条件的记录
        MyApplication.getAccount().downloadShareInfo(this, "更新中，请稍后...", new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if (code != RETURN_CODE_SUCCESS) {
                    Toast.makeText(ShareManageActivity.this, WebFailureHandler.toString(code), Toast.LENGTH_SHORT).show();
                }
                MyApplication.getAccount().readShareInfoFromLocalDb();
                updateView();
            }
        });
    }

    private void updateView() {
        shareInfoAdapter.notifyDataSetChanged();
    }
}