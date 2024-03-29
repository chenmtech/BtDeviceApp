package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.global.AppConstant.SUPPORT_RECORD_TYPES;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.RecordListAdapter;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.KeyBoardUtil;
import com.cmtech.android.bledeviceapp.view.layout.RecordSearchLayout;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
  *
  * ClassName:      RecordExplorerActivity
  * Description:    记录浏览Activity
  * Author:         chenm
  * CreateDate:     2018/11/10 下午5:34
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午5:34
  * UpdateRemark:   优化代码
  * Version:        1.0
 */

public class RecordExplorerActivity extends AppCompatActivity {
    private static final String TAG = "RecordExplorerActivity";

    // 打开记录返回码
    private static final int RC_OPEN_RECORD = 1;

    // 缺省单次要下载的记录数
    private static final int DEFAULT_DOWNLOAD_RECORD_NUM_PER_TIME = 20;

    // 记录列表
    private final List<BasicRecord> recordList = new ArrayList<>();

    private RecordListAdapter recordAdapter; // Adapter
    private RecyclerView recordView; // RecycleView
    private TextView tvNoRecord; // no record

    private RecordSearchLayout searchLayout; // 搜索记录的Layout

    private RecordType recordType = null; // 当前浏览的记录类型
    private String filterStr = ""; // 过滤的备注中包含的字符串
    private long filterTime = new Date().getTime(); // 过滤的记录时间


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_explorer);

        // create ToolBar
        Toolbar toolbar = findViewById(R.id.tb_record_explorer);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        searchLayout = findViewById(R.id.layout_record_search);
        searchLayout.setActivity(this);

        // 初始化并设置“记录类型”的spinner
        Spinner typeSpinner = findViewById(R.id.spinner_record_type);
        List<Map<String, Object>> spinnerItems = new ArrayList<>();
        for (RecordType type : SUPPORT_RECORD_TYPES) {
            Map<String, Object> item = new HashMap<>();
            item.put("icon", type.getIconId());
            item.put("name", type.getName());
            spinnerItems.add(item);
        }
        SimpleAdapter simpleadapter = new SimpleAdapter(this, spinnerItems,
                R.layout.recycle_item_record_type, new String[] { "icon", "name" },
                new int[] {R.id.iv_icon,R.id.tv_name_or_id});
        typeSpinner.setAdapter(simpleadapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setRecordType(SUPPORT_RECORD_TYPES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 初始化记录列表
        recordView = findViewById(R.id.rv_record_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recordView.setLayoutManager(fileLayoutManager);
        recordView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recordAdapter = new RecordListAdapter(this, recordList);
        recordView.setAdapter(recordAdapter);
        recordView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //用来标记是否正在向上滑动
            private boolean isSlidingUpward = false;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                // 当不滑动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的itemPosition
                    int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();
                    int itemCount = recordAdapter.getItemCount();

                    // 判断是否滑动到了最后一个item，并且是向上滑动
                    if (lastItemPosition == (itemCount - 1) && isSlidingUpward) {
                        //加载更多
                        updateRecordList();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 大于0表示正在向上滑动，小于等于0表示停止或向下滑动
                isSlidingUpward = dy > 0;
            }
        });

        tvNoRecord = findViewById(R.id.tv_no_record);
        tvNoRecord.setText(R.string.no_record);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        } else if(id == R.id.search_record) { // 搜索记录
            searchRecords(recordType, searchLayout.getSearchString(), searchLayout.getSearchTime());
        } else if(id == R.id.filter_reset) { // 重置过滤条件
            if(searchLayout != null) {
                searchLayout.resetFilterCondition();
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 打开记录的返回码
        if(requestCode == RC_OPEN_RECORD) {
            if(resultCode == RESULT_OK) {
                recordAdapter.notifySelectedItemChanged(); // 更新adapter
            } else {
                Toast.makeText(this, "打开记录错误。", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 搜索记录
    public void searchRecords(String filterStr, long filterTime) {
        searchRecords(recordType, filterStr, filterTime);
    }

    // 打开记录
    public void openRecord(int index) {
        int recordId = recordList.get(index).getId();
        List<IDevice> openedDevice = MyApplication.getDeviceManager().getOpenedDevice();
        for(IDevice device : openedDevice) {
            if(device.getRecordingRecord() != null && recordId == device.getRecordingRecord().getId()) {
                Toast.makeText(this, "正在记录中，不能打开。", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        recordAdapter.setSelectedRecord(index);
        updateView();
        doOpenRecord(recordList.get(index));
    }

    private void doOpenRecord(BasicRecord record) {
        Class<? extends Activity> actClass = RecordType.fromCode(record.getTypeCode()).getActivityClass();
        if (actClass != null) {
            Intent intent = new Intent(RecordExplorerActivity.this, actClass);
            intent.putExtra("record_id", record.getId());
            startActivityForResult(intent, RC_OPEN_RECORD);
        }
    }

    // 删除记录
    public void deleteRecord(int index) {
        BasicRecord record = recordList.get(index);
        if(record != null) {
            int recordId = record.getId();
            List<IDevice> openedDevice = MyApplication.getDeviceManager().getOpenedDevice();
            for(IDevice device : openedDevice) {
                if(device.getRecordingRecord() != null && recordId == device.getRecordingRecord().getId()) {
                    Toast.makeText(this, "正在记录中，不能删除。", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delete_record).setMessage(R.string.really_wanna_delete_record);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    record.delete(RecordExplorerActivity.this, new ICodeCallback() {
                        @Override
                        public void onFinish(int code, String msg) {
                            if (recordList.remove(record)) {
                                recordAdapter.unselected();
                                updateView();
                            }
                        }
                    });
                }
            }).show();
        }
    }

    // 上传/更新记录
    public void uploadRecord(int index) {
        int recordId = recordList.get(index).getId();
        List<IDevice> openedDevice = MyApplication.getDeviceManager().getOpenedDevice();
        for(IDevice device : openedDevice) {
            if(device.getRecordingRecord() != null && recordId == device.getRecordingRecord().getId()) {
                Toast.makeText(this, "正在记录中，不能上传。", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        BasicRecord record = LitePal.find(recordList.get(index).getClass(), recordId, true);
        recordList.set(index, record);
        record.upload(this, new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                Toast.makeText(RecordExplorerActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (code == RCODE_SUCCESS) {
                    updateView();
                }
            }
        });
    }

    private void setRecordType(final RecordType recordType) {
        if(recordType == null) return;
        searchRecords(recordType, searchLayout.getSearchString(), searchLayout.getSearchTime());
    }

    private void searchRecords(RecordType recordType, String noteFilterStr, long updateTime) {
        this.recordType = recordType;
        this.filterStr = noteFilterStr.trim();
        this.filterTime = updateTime;

        recordList.clear();
        recordAdapter.unselected();
        updateView();

        updateRecordList();
    }

    // 更新记录列表
    private void updateRecordList() {
        KeyBoardUtil.closeKeybord(this);

        // 从服务器下载满足条件的记录保存到本地数据库，之后再从本地数据库中读取满足条件的记录
        BasicRecord.downloadRecords(this, recordType, MyApplication.getAccountId(),
                DEFAULT_DOWNLOAD_RECORD_NUM_PER_TIME, filterStr, filterTime, new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                if(code != RCODE_SUCCESS) {
                    Toast.makeText(RecordExplorerActivity.this, msg, Toast.LENGTH_SHORT).show();
                }

                // 从本地读取记录
                List<? extends BasicRecord> records = BasicRecord.readRecordsFromLocalDb(recordType, MyApplication.getAccountId(), filterTime, filterStr, DEFAULT_DOWNLOAD_RECORD_NUM_PER_TIME);

                if(records == null) {
                    Toast.makeText(RecordExplorerActivity.this, R.string.no_more, Toast.LENGTH_SHORT).show();
                } else {
                    filterTime = records.get(records.size() - 1).getCreateTime(); // 用最后一条记录的创建时间更新过滤时间，准备下次查询
                    recordList.addAll(records);
                    updateView();
                }
            }
        });
    }

    private void updateView() {
        if(recordList.isEmpty()) {
            recordView.setVisibility(View.INVISIBLE);
            tvNoRecord.setVisibility(View.VISIBLE);
        }else {
            recordView.setVisibility(View.VISIBLE);
            tvNoRecord.setVisibility(View.INVISIBLE);
        }
        recordAdapter.notifyDataSetChanged();
    }
}
