package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_APPLY_NEW_SHARE;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.WebAsyncTask;
import com.cmtech.android.bledeviceapp.model.WebResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 用来管理分享信息的Activity
 */
public class ShareManageActivity extends AppCompatActivity {
    private ShareInfoAdapter siAdapter;
    private RecyclerView rvShareInfo;
    private EditText etShareToId;
    private Button btnApplyShare;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_manage);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_share_manage);
        setSupportActionBar(toolbar);

        // 初始化分享信息列表
        rvShareInfo = findViewById(R.id.rv_share_info);
        rvShareInfo.setLayoutManager(new LinearLayoutManager(this));
        rvShareInfo.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        siAdapter = new ShareInfoAdapter(MyApplication.getShareInfoList());
        rvShareInfo.setAdapter(siAdapter);

        etShareToId = findViewById(R.id.et_share_to_id);

        btnApplyShare = findViewById(R.id.btn_apply_share);
        btnApplyShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = etShareToId.getText().toString();
                int id = INVALID_ID;
                try{
                    id = Integer.parseInt(idStr);
                } catch (Exception ignored) {
                }
                if(id != INVALID_ID) {
                    applyNewShare(id);
                } else {
                    Toast.makeText(ShareManageActivity.this, "无效ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
    public void updateShareInfoList() {
        // 从服务器下载分享信息保存到本地数据库，之后再从分享信息中下载联系人信息
        MyApplication.getAccount().downloadShareInfos(this, "更新中，请稍后...", new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                if (code == RCODE_SUCCESS) {
                    List<Integer> cpIds = MyApplication.getAccount().extractContactPeopleIdsFromShareInfos();
                    MyApplication.getAccount().downloadContactPeopleInfos(ShareManageActivity.this, null,
                            cpIds, new ICodeCallback() {
                                @Override
                                public void onFinish(int code, String msg) {
                                    if(code == RCODE_SUCCESS) {
                                        updateView();
                                    } else {
                                        Toast.makeText(ShareManageActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(ShareManageActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateView() {
        siAdapter.notifyDataSetChanged();
    }

    private void applyNewShare(int id) {
        MyApplication.getAccount().applyNewShare(this, id, new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                Toast.makeText(ShareManageActivity.this, msg, Toast.LENGTH_SHORT).show();
                if(code==RCODE_SUCCESS) {
                    updateShareInfoList();
                }
            }
        });
    }
}