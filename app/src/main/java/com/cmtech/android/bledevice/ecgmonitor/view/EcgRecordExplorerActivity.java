package com.cmtech.android.bledevice.ecgmonitor.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgRecordListAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgRecord;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgRecordExplorer;
import com.cmtech.android.bledeviceapp.R;

import java.util.List;

/**
  *
  * ClassName:      EcgRecordExplorerActivity
  * Description:    Ecg记录浏览Activity
  * Author:         chenm
  * CreateDate:     2018/11/10 下午5:34
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午5:34
  * UpdateRemark:   制作类图，优化代码
  * Version:        1.0
 */

public class EcgRecordExplorerActivity extends AppCompatActivity implements EcgRecordExplorer.OnEcgRecordsListener {
    private static final String TAG = "EcgRecordExplorerActivity";
    private static final int DEFAULT_LOAD_RECORD_NUM_EACH_TIMES = 10; // 缺省每次加载的记录数

    private EcgRecordExplorer explorer;      // 记录浏览器实例
    private EcgRecordListAdapter fileAdapter; // 记录Adapter
    private RecyclerView rvRecords; // 记录RecycleView
    private TextView tvPromptInfo; // 提示信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg_record_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_ecg_record_explorer);
        setSupportActionBar(toolbar);

        explorer = new EcgRecordExplorer(EcgRecordExplorer.ORDER_MODIFY_TIME, this);

        rvRecords = findViewById(R.id.rv_ecg_record_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvRecords.setLayoutManager(fileLayoutManager);
        rvRecords.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgRecordListAdapter(this, explorer.getRecordList(), explorer.getUpdatedRecords(), explorer.getSelectedRecord());
        rvRecords.setAdapter(fileAdapter);
        rvRecords.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == fileAdapter.getItemCount()-1) {
                    //explorer.loadNextRecords(DEFAULT_LOAD_RECORD_NUM_EACH_TIMES);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if(layoutManager != null)
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });

        tvPromptInfo = findViewById(R.id.tv_prompt_info);
        tvPromptInfo.setText("无信号");

        onRecordListChanged(explorer.getRecordList());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ecgfile_explore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.ecg_record_update:
                importFromWechat();
                break;

            case R.id.ecg_record_delete:
                deleteSelectedFile();
                break;

            case R.id.share_with_wechat:
                shareSelectedFileThroughWechat();
                break;

        }
        return true;
    }

    private void importFromWechat() {
        boolean updated = explorer.importFromWechat();

        if(updated) {
            fileAdapter.clear();
            tvPromptInfo.setText("正在载入信号");
            /*new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(explorer.loadNextRecords(DEFAULT_LOAD_RECORD_NUM_EACH_TIMES) == 0) {
                        tvPromptInfo.setText("无信号可载入。");
                    }
                }
            });*/
        }
    }

    public void deleteSelectedFile() {
        explorer.deleteSelectRecord(this);
    }

    public void shareSelectedFileThroughWechat() {
        explorer.shareSelectedFileThroughWechat(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        explorer.close();
    }

    public void selectFile(final EcgRecord ecgFile) {
        explorer.selectRecord(ecgFile);
        Intent intent = new Intent(this, EcgRecordActivity.class);
        intent.putExtra("record_id", ecgFile.getId());
        startActivityForResult(intent, 1);
    }

    public List<EcgRecord> getUpdatedFiles() {
        return explorer.getUpdatedRecords();
    }

    @Override
    public void onRecordSelected(final EcgRecord selectedRecord) {
        fileAdapter.updateSelectedFile(selectedRecord);
    }

    @Override
    public void onRecordAdded(final EcgRecord ecgRecord) {
        if(ecgRecord != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rvRecords.setVisibility(View.VISIBLE);
                    tvPromptInfo.setVisibility(View.INVISIBLE);
                    fileAdapter.insertNewFile(ecgRecord);
                }
            });
        }
    }

    @Override
    public void onRecordListChanged(final List<EcgRecord> recordList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(recordList == null || recordList.isEmpty()) {
                    rvRecords.setVisibility(View.INVISIBLE);
                    tvPromptInfo.setVisibility(View.VISIBLE);
                }else {
                    rvRecords.setVisibility(View.VISIBLE);
                    tvPromptInfo.setVisibility(View.INVISIBLE);
                }

                fileAdapter.updateFileList(recordList, getUpdatedFiles());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: // 心电记录返回
                if(resultCode == RESULT_OK) {
                    boolean updated = data.getBooleanExtra("updated", false);
                    if(updated) {
                        explorer.addUpdatedRecord(explorer.getSelectedRecord());
                        fileAdapter.notifyDataSetChanged();
                    }
                }
                break;

        }
    }
}
