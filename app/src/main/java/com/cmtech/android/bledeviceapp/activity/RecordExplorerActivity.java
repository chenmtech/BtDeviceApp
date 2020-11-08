package com.cmtech.android.bledeviceapp.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.RecordListAdapter;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordFactory;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.KeyBoardUtil;
import com.cmtech.android.bledeviceapp.view.layout.RecordSearchLayout;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.DEFAULT_RECORD_VER;
import static com.cmtech.android.bledeviceapp.data.report.EcgReport.INVALID_TIME;
import static com.cmtech.android.bledeviceapp.global.AppConstant.SUPPORT_RECORD_TYPES;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;

/**
  *
  * ClassName:      RecordExplorerActivity
  * Description:    记录浏览Activity
  * Author:         chenm
  * CreateDate:     2018/11/10 下午5:34
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午5:34
  * UpdateRemark:   制作类图，优化代码
  * Version:        1.0
 */

public class RecordExplorerActivity extends AppCompatActivity {
    private static final String TAG = "RecordExplorerActivity";
    private static final int RC_OPEN_RECORD = 1;
    private static final int DEFAULT_DOWNLOAD_RECORD_NUM = 20;

    private final List<BasicRecord> allRecords = new ArrayList<>(); // all records
    private RecordListAdapter recordAdapter; // Adapter
    private RecyclerView recordView; // RecycleView
    private TextView tvNoRecord; // no record
    private RecordSearchLayout searchLayout;

    private RecordType recordType = null; // record type in record list
    private String noteFilterStr = ""; // record note filter string
    private long updateTime = new Date().getTime(); // update time in record list

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
        searchLayout.setExplorerActivity(this);

        // init record type spinner
        Spinner typeSpinner = findViewById(R.id.spinner_record_type);
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (RecordType type : SUPPORT_RECORD_TYPES) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("icon", type.getIconId());
            item.put("name", type.getName());
            items.add(item);
        }
        SimpleAdapter simpleadapter = new SimpleAdapter(this, items,
                R.layout.recycle_item_record_type, new String[] { "icon", "name" },
                new int[] {R.id.iv_icon,R.id.tv_name});
        typeSpinner.setAdapter(simpleadapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setRecordType(SUPPORT_RECORD_TYPES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // init record list
        recordView = findViewById(R.id.rv_record_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recordView.setLayoutManager(fileLayoutManager);
        recordView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recordAdapter = new RecordListAdapter(this, allRecords);
        recordView.setAdapter(recordAdapter);
        recordView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //用来标记是否正在向上滑动
            private boolean isSlidingUpward = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
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
        } else if(id == R.id.search_record) {
            if(searchLayout.getVisibility() == View.VISIBLE)
                searchLayout.setVisibility(View.GONE);
            else
                searchLayout.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_OPEN_RECORD) {
            if(resultCode == RESULT_OK) {
                recordAdapter.notifySelectedItemChanged();
            } else {
                Toast.makeText(this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ViseLog.e("RecordExplorerActivity onDestroy");
    }

    public void searchRecords(String noteFilterStr, long updateTime) {
        searchRecords(recordType, noteFilterStr, updateTime);
    }

    public void openRecord(BasicRecord record) {
        if(record != null) {
            Intent intent = null;
            Class<? extends Activity> actClass = RecordType.fromCode(record.getTypeCode()).getActivityClass();
            if (actClass != null) {
                intent = new Intent(RecordExplorerActivity.this, actClass);
            }
            if (intent != null) {
                intent.putExtra("record_id", record.getId());
                startActivityForResult(intent, RC_OPEN_RECORD);
            }
        }
    }

    public void deleteRecord(final BasicRecord record) {
        if(record != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delete_record).setMessage(R.string.really_wanna_delete_record);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    record.delete(RecordExplorerActivity.this, new ICodeCallback() {
                        @Override
                        public void onFinish(int code) {
                            if (allRecords.remove(record)) {
                                recordAdapter.unselected();
                                updateRecordView();
                            }
                        }
                    });
                }
            }).setNegativeButton(R.string.cancel, null).show();
        }
    }

    public void uploadRecord(final BasicRecord record) {
        record.upload(this, new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if (code == RETURN_CODE_SUCCESS) {
                    updateRecordView();
                } else {
                    Toast.makeText(RecordExplorerActivity.this, "上传记录出错。", Toast.LENGTH_SHORT).show();
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
        this.noteFilterStr = noteFilterStr.trim();
        this.updateTime = updateTime;

        allRecords.clear();
        recordAdapter.unselected();
        updateRecordView();

        updateRecordList();
    }

    private void updateRecordList() {
        KeyBoardUtil.closeKeybord(this);

        BasicRecord record = RecordFactory.create(recordType, DEFAULT_RECORD_VER, INVALID_TIME, null, MyApplication.getAccountId());
        if(record == null) {
            ViseLog.e("The record type is not supported.");
            return;
        }

        record.retrieveList(this, DEFAULT_DOWNLOAD_RECORD_NUM, noteFilterStr, updateTime, new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if(code == RETURN_CODE_WEB_FAILURE) {
                    Toast.makeText(RecordExplorerActivity.this, "网络错误，只能加载本地记录。", Toast.LENGTH_SHORT).show();
                }

                List<? extends BasicRecord> records = BasicRecord.retrieveListFromLocalDb(recordType, MyApplication.getAccount(), updateTime, noteFilterStr, DEFAULT_DOWNLOAD_RECORD_NUM);

                if(records == null) {
                    Toast.makeText(RecordExplorerActivity.this, R.string.no_more, Toast.LENGTH_SHORT).show();
                } else {
                    updateTime = records.get(records.size() - 1).getCreateTime();
                    allRecords.addAll(records);
                    updateRecordView();
                }
            }
        });
    }

    private void updateRecordView() {
        if(allRecords.isEmpty()) {
            recordView.setVisibility(View.INVISIBLE);
            tvNoRecord.setVisibility(View.VISIBLE);
        }else {
            recordView.setVisibility(View.VISIBLE);
            tvNoRecord.setVisibility(View.INVISIBLE);
        }
        recordAdapter.notifyDataSetChanged();
    }
}
