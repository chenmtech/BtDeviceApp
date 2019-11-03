package com.cmtech.android.bledevice.ecgmonitor.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgRecordListAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgRecord;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgRecordExplorer;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.DIR_ECG_SIGNAL;

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

    private static final int DEFAULT_LOADED_FILENUM_EACH_TIMES = 10; // 缺省每次加载的文件数

    private EcgRecordExplorer explorer;      // 文件浏览器实例
    private EcgRecordListAdapter fileAdapter; // 文件Adapter
    private RecyclerView rvFiles; // 文件RecycleView
    private TextView tvPromptInfo; // 提示信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg_record_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_ecg_record_explorer);
        setSupportActionBar(toolbar);

        try {
            explorer = new EcgRecordExplorer(DIR_ECG_SIGNAL, EcgRecordExplorer.ORDER_MODIFY_TIME, this);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "心电记录目录错误。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvFiles = findViewById(R.id.rv_ecg_record_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvFiles.setLayoutManager(fileLayoutManager);
        rvFiles.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgRecordListAdapter(this, explorer.getRecordList(), explorer.getUpdatedRecords(), explorer.getSelectedRecord());
        rvFiles.setAdapter(fileAdapter);
        rvFiles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == fileAdapter.getItemCount()-1) {
                    explorer.loadNextRecords(DEFAULT_LOADED_FILENUM_EACH_TIMES);
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
        tvPromptInfo.setText("正在载入信号");
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(explorer.loadNextRecords(DEFAULT_LOADED_FILENUM_EACH_TIMES) == 0) {
                    tvPromptInfo.setText("无信号可载入。");
                }
            }
        });
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
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(explorer.loadNextRecords(DEFAULT_LOADED_FILENUM_EACH_TIMES) == 0) {
                        tvPromptInfo.setText("无信号可载入。");
                    }
                }
            });
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

    public void selectFile(final EcgFile ecgFile) {
        explorer.selectFile(ecgFile);
        Intent intent = new Intent(this, EcgRecordActivity.class);
        intent.putExtra("file_name", ecgFile.getFileName());
        startActivityForResult(intent, 1);
    }

    public List<File> getUpdatedFiles() {
        return explorer.getUpdatedRecords();
    }

    @Override
    public void onRecordSelected(final EcgRecord selectedRecord) {
        fileAdapter.updateSelectedFile(selectedRecord);
    }

    @Override
    public void onNewRecordAdded(final EcgRecord ecgRecord) {
        if(ecgRecord != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rvFiles.setVisibility(View.VISIBLE);
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
                    rvFiles.setVisibility(View.INVISIBLE);
                    tvPromptInfo.setVisibility(View.VISIBLE);
                }else {
                    rvFiles.setVisibility(View.VISIBLE);
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
                        explorer.addUpdatedFile(explorer.getSelectedRecord().getFile());
                        fileAdapter.notifyDataSetChanged();
                    }
                }
                break;

        }
    }
}
