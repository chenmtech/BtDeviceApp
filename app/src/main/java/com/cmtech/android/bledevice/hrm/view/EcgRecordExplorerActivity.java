package com.cmtech.android.bledevice.hrm.view;

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

import com.cmtech.android.bledevice.hrm.model.BleEcgRecord10;
import com.cmtech.android.bledevice.hrm.model.EcgRecordListAdapter;
import com.cmtech.android.bledeviceapp.R;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;

public class EcgRecordExplorerActivity extends AppCompatActivity {
    private static final String TAG = "EcgRecordExplorerActivity";

    private List<BleEcgRecord10> allRecords = new ArrayList<>(); // 所有心电记录列表
    private EcgRecordListAdapter recordAdapter; // 记录Adapter
    private RecyclerView rvRecords; // 记录RecycleView
    private TextView tvPromptInfo; // 提示信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg_record_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_ecg_record_explorer);
        setSupportActionBar(toolbar);

        rvRecords = findViewById(R.id.rv_ecg_record_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvRecords.setLayoutManager(fileLayoutManager);
        rvRecords.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recordAdapter = new EcgRecordListAdapter(this, allRecords);
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

        LitePal.select("createTime, devAddress, creatorPlat, creatorId, recordSecond").order("createTime desc").findAsync(BleEcgRecord10.class).listen(new FindMultiCallback<BleEcgRecord10>() {
            @Override
            public void onFinish(List<BleEcgRecord10> list) {
                if(list != null)
                    allRecords.addAll(list);
                updateRecordList();
            }
        });
    }

    public void selectRecord(final BleEcgRecord10 record) {
        if(record != null) {
            Intent intent = new Intent(EcgRecordExplorerActivity.this, EcgRecordActivity.class);
            intent.putExtra("record_id", record.getId());
            startActivity(intent);
        }
    }

    public void deleteRecord(final BleEcgRecord10 record) {
        if(record != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("删除心率记录").setMessage("确定删除该心率记录吗？");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(allRecords.remove(record)) {
                        updateRecordList();
                    }
                    LitePal.delete(BleEcgRecord10.class, record.getId());
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }
    }

    private void updateRecordList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(allRecords == null || allRecords.isEmpty()) {
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
