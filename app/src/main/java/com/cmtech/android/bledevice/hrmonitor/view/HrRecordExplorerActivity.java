package com.cmtech.android.bledevice.hrmonitor.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

import com.cmtech.android.bledevice.ecg.activity.EcgRecordActivity;
import com.cmtech.android.bledevice.ecg.adapter.EcgRecordListAdapter;
import com.cmtech.android.bledevice.ecg.record.EcgRecord;
import com.cmtech.android.bledevice.ecg.record.EcgRecordExplorer;
import com.cmtech.android.bledevice.hrmonitor.model.BleHrRecord10;
import com.cmtech.android.bledevice.hrmonitor.model.HrRecordExplorer;
import com.cmtech.android.bledevice.hrmonitor.model.HrRecordListAdapter;
import com.cmtech.android.bledeviceapp.R;

import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

/**
  *
  * ClassName:      HrRecordExplorerActivity
  * Description:    Ecg记录浏览Activity
  * Author:         chenm
  * CreateDate:     2018/11/10 下午5:34
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午5:34
  * UpdateRemark:   制作类图，优化代码
  * Version:        1.0
 */

public class HrRecordExplorerActivity extends AppCompatActivity implements HrRecordExplorer.OnHrRecordsListener {
    private static final String TAG = "EcgRecordExplorerActivity";
    private static final int DEFAULT_LOAD_RECORD_NUM_EACH_TIMES = 10; // 缺省每次加载的记录数

    private HrRecordExplorer explorer;      // 记录浏览器实例
    private HrRecordListAdapter recordAdapter; // 记录Adapter
    private RecyclerView rvRecords; // 记录RecycleView
    private TextView tvPromptInfo; // 提示信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_record_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_hr_record_explorer);
        setSupportActionBar(toolbar);

        explorer = new HrRecordExplorer(this);

        rvRecords = findViewById(R.id.rv_hr_record_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvRecords.setLayoutManager(fileLayoutManager);
        rvRecords.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recordAdapter = new HrRecordListAdapter(this, explorer.getAllRecords(), explorer.getSelRecord());
        rvRecords.setAdapter(recordAdapter);
        rvRecords.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == recordAdapter.getItemCount()-1) {
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
        tvPromptInfo.setText("无记录");

        onRecordListChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ecg_record_explore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.ecg_record_delete:
                deleteSelectedRecord();
                break;

        }
        return true;
    }

    public void deleteSelectedRecord() {
        BleHrRecord10 selectedRecord = explorer.getSelRecord();
        if(selectedRecord != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("删除心电记录").setMessage("确定删除该心电记录吗？");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    explorer.deleteSelRecord();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        explorer.close();
    }

    public void selectRecord(final BleHrRecord10 record) {
        explorer.selectRecord(record);
    }

    @Override
    public void onRecordSelected(final BleHrRecord10 selectedRecord) {
        recordAdapter.updateSelectedFile(selectedRecord);
        if(selectedRecord != null) {
            Intent intent = new Intent(this, EcgRecordActivity.class);
            intent.putExtra("record_id", selectedRecord.getId());
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onRecordListChanged() {
        final List<BleHrRecord10> recordList = explorer.getAllRecords();
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

                recordAdapter.updateRecordList();
            }
        });
    }
}
