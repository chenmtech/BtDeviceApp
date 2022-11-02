package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

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
import com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.ContactPerson;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;

import java.util.List;

/**
 * 用来管理分享信息的Activity
 */
public class ShareManageActivity extends AppCompatActivity {
    private ShareInfoAdapter shareInfoAdapter;
    private RecyclerView rvShareInfo;
    private EditText etShareId;
    private Button btnAddShare;


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
        shareInfoAdapter = new ShareInfoAdapter(MyApplication.getShareInfoList());
        rvShareInfo.setAdapter(shareInfoAdapter);

        etShareId = findViewById(R.id.et_share_id);

        btnAddShare = findViewById(R.id.btn_add_share);
        btnAddShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = etShareId.getText().toString();
                int id = INVALID_ID;
                try{
                    id = Integer.parseInt(idStr);
                } catch (Exception ignored) {
                }
                if(id != INVALID_ID) {
                    addShare(id);
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
        // 从服务器下载满足条件的记录保存到本地数据库，之后再从本地数据库中读取满足条件的记录
        MyApplication.getAccount().downloadShareInfos(this, "更新中，请稍后...", new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if (code == RETURN_CODE_SUCCESS) {
                    List<Integer> cpIds = MyApplication.getAccount().extractContactPeopleIdsFromShareInfos();
                    for(int id : cpIds) {
                        ContactPerson cp = MyApplication.getAccount().getContactPerson(id);
                        MyApplication.getAccount().downloadContactPerson(ShareManageActivity.this, null,
                                id, new ICodeCallback() {
                                    @Override
                                    public void onFinish(int code) {
                                        if(code == RETURN_CODE_SUCCESS) {
                                            MyApplication.getAccount().readContactPeopleFromLocalDb();
                                            updateView();
                                        }
                                    }
                                });
                    }
                    updateView();
                } else {
                    Toast.makeText(ShareManageActivity.this, WebFailureHandler.toString(code), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateView() {
        shareInfoAdapter.notifyDataSetChanged();
    }

    private void addShare(int id) {
        if(MyApplication.getAccountId() == id) return;
        new AccountAsyncTask(this, "请稍等", AccountAsyncTask.CMD_ADD_SHARE_INFO,
                new Object[]{id}, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if(code == RETURN_CODE_SUCCESS) {
                    updateShareInfoList();
                    Toast.makeText(ShareManageActivity.this, "已申请", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShareManageActivity.this, "申请无效", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(MyApplication.getAccount());
    }
}