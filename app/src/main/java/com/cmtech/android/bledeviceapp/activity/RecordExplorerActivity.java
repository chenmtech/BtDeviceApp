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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledevice.record.RecordFactory;
import com.cmtech.android.bledevice.record.RecordType;
import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledevice.record.BleHrRecord10;
import com.cmtech.android.bledevice.record.RecordWebAsyncTask;
import com.cmtech.android.bledevice.hrm.view.EcgRecordActivity;
import com.cmtech.android.bledevice.hrm.view.HrRecordActivity;
import com.cmtech.android.bledevice.record.AbstractRecord;
import com.cmtech.android.bledevice.record.BleThermoRecord10;
import com.cmtech.android.bledevice.thermo.view.ThermoRecordActivity;
import com.cmtech.android.bledevice.record.BleTempHumidRecord10;
import com.cmtech.android.bledeviceapp.MyApplication;
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
import java.util.List;

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

    private long updateTime;

    private List<IRecord> allRecords = new ArrayList<>(); // all records
    private RecordListAdapter adapter; // Adapter
    private RecyclerView view; // RecycleView
    private TextView tvPromptInfo; // prompt info
    private Spinner recordSpinner;

    private RecordType recordType = UNKNOWN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_record_explorer);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recordSpinner = findViewById(R.id.spinner_record);
        ArrayAdapter<CharSequence> recordAdapter = ArrayAdapter.createFromResource(this,
                R.array.record_array, android.R.layout.simple_spinner_item);
        recordSpinner.setAdapter(recordAdapter);
        recordSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeRecordType(RecordType.getType(position+1));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        view = findViewById(R.id.rv_record_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        view.setLayoutManager(fileLayoutManager);
        view.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new RecordListAdapter(this, allRecords);
        view.setAdapter(adapter);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == adapter.getItemCount()-1) {
                    updateRecords(updateTime);
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

        //setRecordType(ECG);
    }

    private void updateRecords(long fromTime) {
        IRecord record = RecordFactory.create(recordType, fromTime, null, AccountManager.getAccount());

        new RecordWebAsyncTask(this, RecordWebAsyncTask.RECORD_DOWNLOAD_INFO_CMD, new RecordWebAsyncTask.RecordWebCallback() {
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

                List<IRecord> newRecords = RecordFactory.createFromLocalDb(recordType, AccountManager.getAccount(), updateTime, DOWNLOAD_NUM_PER_TIME);
                if(newRecords != null && !newRecords.isEmpty()) {
                    updateTime = newRecords.get(newRecords.size() - 1).getCreateTime();
                    allRecords.addAll(newRecords);
                    ViseLog.e(allRecords.toString());
                }

                updateRecordView();
            }
        }).execute(record);
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

            case R.id.ecg_record:
                changeRecordType(ECG);
                break;

            case R.id.hr_record:
                changeRecordType(HR);
                break;

            case R.id.thm_record:
                changeRecordType(TH);
                break;

            case R.id.thermo_record:
                changeRecordType(THERMO);
                break;
        }
        return true;
    }

    public void changeRecordType(RecordType type) {
        if(this.recordType == type) return;
        setRecordType(type);
    }

    public void setRecordType(RecordType type) {
        this.recordType = type;
        updateTime = new Date().getTime();
        allRecords.clear();
        updateRecords(updateTime);
        updateRecordView();
    }

    public void selectRecord(final IRecord record) {
        openRecordActivity(record);
    }

    private void openRecordActivity(IRecord record) {
        Intent intent = null;
        if (record instanceof BleHrRecord10) {
            intent = new Intent(RecordExplorerActivity.this, HrRecordActivity.class);
        } else if (record instanceof BleEcgRecord10) {
            intent = new Intent(RecordExplorerActivity.this, EcgRecordActivity.class);
        } else if (record instanceof BleThermoRecord10) {
            intent = new Intent(RecordExplorerActivity.this, ThermoRecordActivity.class);
        }
        if (intent != null) {
            intent.putExtra("record_id", record.getId());
            startActivity(intent);
        }
    }

    public void deleteRecord(final IRecord record) {
        if(record != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("删除记录").setMessage("确定删除该记录吗？");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(allRecords.remove(record)) {
                        updateRecordView();
                    }
                    if(record instanceof BleHrRecord10) {
                        LitePal.delete(BleHrRecord10.class, record.getId());
                        new RecordWebAsyncTask(RecordExplorerActivity.this, RecordWebAsyncTask.RECORD_DELETE_CMD, new RecordWebAsyncTask.RecordWebCallback() {
                            @Override
                            public void onFinish(Object[] objs) {
                                MyApplication.showMessageUsingShortToast((Integer)objs[0]+(String)objs[1]);
                            }
                        }).execute(record);
                    } else if(record instanceof BleEcgRecord10) {
                        LitePal.delete(BleEcgRecord10.class, record.getId());
                        new RecordWebAsyncTask(RecordExplorerActivity.this, RecordWebAsyncTask.RECORD_DELETE_CMD, new RecordWebAsyncTask.RecordWebCallback() {
                            @Override
                            public void onFinish(Object[] objs) {
                                MyApplication.showMessageUsingShortToast((Integer)objs[0]+(String)objs[1]);
                            }
                        }).execute(record);
                    } else if(record instanceof BleThermoRecord10) {
                        LitePal.delete(BleThermoRecord10.class, record.getId());
                    } else if(record instanceof BleTempHumidRecord10) {
                        LitePal.delete(BleTempHumidRecord10.class, record.getId());
                    }
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
            view.setVisibility(View.INVISIBLE);
            tvPromptInfo.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.VISIBLE);
            tvPromptInfo.setVisibility(View.INVISIBLE);
        }
        adapter.updateRecordList();
    }
}
