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
import android.view.View;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrmonitor.model.BleEcgRecord10;
import com.cmtech.android.bledevice.hrmonitor.model.BleHrRecord10;
import com.cmtech.android.bledevice.hrmonitor.model.HrRecordListAdapter;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

public class HrRecordExplorerActivity extends AppCompatActivity {
    private static final String TAG = "HrRecordExplorerActivity";
    private static final int DEFAULT_LOAD_RECORD_NUM_EACH_TIMES = 10; // 缺省每次加载的记录数

    private List<BleHrRecord10> allRecords = new ArrayList<>(); // 所有心电记录列表
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

        rvRecords = findViewById(R.id.rv_hr_record_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvRecords.setLayoutManager(fileLayoutManager);
        rvRecords.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recordAdapter = new HrRecordListAdapter(this, allRecords);
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
                if(layoutManager != null) {
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    if(lastVisibleItem == recordAdapter.getItemCount()-1) {

                    }
                }
            }
        });

        tvPromptInfo = findViewById(R.id.tv_prompt_info);
        tvPromptInfo.setText("无记录");
        //"id, hrHist, createTime, devAddress, creatorPlat, creatorId"
        LitePal.select("createTime, devAddress, creatorPlat, creatorId, saveTime").order("createTime desc").findAsync(BleHrRecord10.class, true).listen(new FindMultiCallback<BleHrRecord10>() {
            @Override
            public void onFinish(List<BleHrRecord10> list) {
                if(list != null)
                    allRecords.addAll(list);
                updateRecordList();
            }
        });
    }

    public void selectRecord(final BleHrRecord10 record) {
        if(record != null) {
            Intent intent = new Intent(this, HrRecordActivity.class);
            intent.putExtra("record_id", record.getId());
            startActivity(intent);
        }
    }

    public void deleteRecord(final BleHrRecord10 record) {
        if(record != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("删除心率记录").setMessage("确定删除该心率记录吗？");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(allRecords.remove(record)) {
                        updateRecordList();
                    }
                    LitePal.delete(BleHrRecord10.class, record.getId());
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }
    }

    private void updateRecordList() {
        if(allRecords.isEmpty()) {
            rvRecords.setVisibility(View.INVISIBLE);
            tvPromptInfo.setVisibility(View.VISIBLE);
        }else {
            rvRecords.setVisibility(View.VISIBLE);
            tvPromptInfo.setVisibility(View.INVISIBLE);
        }
        recordAdapter.updateRecordList();
    }
}
