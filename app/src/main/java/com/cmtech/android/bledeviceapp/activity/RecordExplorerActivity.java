package com.cmtech.android.bledeviceapp.activity;

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
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.AbstractRecord;
import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledevice.record.RecordFactory;
import com.cmtech.android.bledevice.record.RecordType;
import com.cmtech.android.bledevice.record.RecordWebAsyncTask;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.RecordListAdapter;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.vise.log.ViseLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cmtech.android.bledevice.record.RecordType.ECG;
import static com.cmtech.android.bledevice.record.RecordType.HR;
import static com.cmtech.android.bledevice.record.RecordType.TH;
import static com.cmtech.android.bledevice.record.RecordType.THERMO;
import static com.cmtech.android.bledevice.record.RecordType.UNKNOWN;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.DOWNLOAD_NUM_PER_TIME;

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

public class RecordExplorerActivity extends AppCompatActivity {
    private static final String TAG = "RecordExplorerActivity";
    private static final RecordType[] SUPPORT_RECORD_TYPES = new RecordType[]{ECG, HR, THERMO, TH};

    private long updateTime; // update time in record list
    private List<IRecord> allRecords = new ArrayList<>(); // all records
    private RecordListAdapter recordAdapter; // Adapter
    private RecyclerView recordView; // RecycleView
    private TextView tvPromptInfo; // prompt info
    private Spinner typeSpinner;
    private RecordType recordType = UNKNOWN; // record type in record list


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_explorer);

        // create ToolBar
        Toolbar toolbar = findViewById(R.id.tb_record_explorer);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        // init spinner
        typeSpinner = findViewById(R.id.spinner_record);
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (RecordType type : SUPPORT_RECORD_TYPES) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("icon", type.getImgId());
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
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == recordAdapter.getItemCount()-1) {
                    updateRecordsFromServer(updateTime);
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
        tvPromptInfo.setText(R.string.no_record);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_record_explore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.delete:
                deleteRecord(recordAdapter.getSelectedRecord());
                break;
        }
        return true;
    }

    private void setRecordType(RecordType type) {
        if(this.recordType == type) return;
        this.recordType = type;
        allRecords.clear();
        recordAdapter.unselected();
        updateRecordView();

        updateTime = new Date().getTime();
        updateRecordsFromServer(updateTime);
    }

    private void updateRecordsFromServer(final long fromTime) {
        IRecord record = RecordFactory.create(recordType, fromTime, null, AccountManager.getAccount());

        new RecordWebAsyncTask(this, RecordWebAsyncTask.RECORD_INFO_DOWNLOAD_CMD, new RecordWebAsyncTask.RecordWebCallback() {
            @Override
            public void onFinish(Object[] objs) {
                if((Integer) objs[0] == 0) { // download success, save into local records
                    try {
                        JSONArray jsonArr = (JSONArray) objs[2];
                        for(int i = 0; i < jsonArr.length(); i++) {
                            JSONObject json = (JSONObject) jsonArr.get(i);
                            AbstractRecord newRecord = (AbstractRecord) RecordFactory.createFromJson(recordType, json);
                            if(newRecord != null) {
                                newRecord.saveIfNotExist("createTime = ? and devAddress = ?", "" + newRecord.getCreateTime(), newRecord.getDevAddress());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                List<IRecord> records = RecordFactory.createFromLocalDb(recordType, AccountManager.getAccount(), fromTime, DOWNLOAD_NUM_PER_TIME);
                if(records == null || records.isEmpty()) {
                    Toast.makeText(RecordExplorerActivity.this, R.string.no_more, Toast.LENGTH_SHORT).show();
                } else  {
                    updateTime = records.get(records.size() - 1).getCreateTime();
                    allRecords.addAll(records);
                    ViseLog.e(allRecords.toString());
                    updateRecordView();
                }
            }
        }).execute(record);
    }

    public void openRecord(IRecord record) {
        if(record != null) {
            Intent intent = null;
            Class actClass = RecordType.getType(record.getTypeCode()).getActivityClass();
            if (actClass != null) {
                intent = new Intent(RecordExplorerActivity.this, actClass);
            }
            if (intent != null) {
                intent.putExtra("record_id", record.getId());
                startActivity(intent);
            }
        }
    }

    public void deleteRecord(final IRecord record) {
        if(record != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("删除记录").setMessage("确定删除该记录吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new RecordWebAsyncTask(RecordExplorerActivity.this, RecordWebAsyncTask.RECORD_DELETE_CMD, new RecordWebAsyncTask.RecordWebCallback() {
                        @Override
                        public void onFinish(Object[] objs) {
                            LitePal.delete(record.getClass(), record.getId());
                            if(allRecords.remove(record)) {
                                recordAdapter.unselected();
                                updateRecordView();
                            }
                        }
                    }).execute(record);
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }
    }

    private void updateRecordView() {
        if(allRecords.isEmpty()) {
            recordView.setVisibility(View.INVISIBLE);
            tvPromptInfo.setVisibility(View.VISIBLE);
        }else {
            recordView.setVisibility(View.VISIBLE);
            tvPromptInfo.setVisibility(View.INVISIBLE);
        }
        recordAdapter.updateRecordList();
    }
}
