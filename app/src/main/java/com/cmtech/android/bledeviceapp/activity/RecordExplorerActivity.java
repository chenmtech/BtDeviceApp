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
import android.widget.TextView;

import com.cmtech.android.bledevice.hrm.model.BleEcgRecord10;
import com.cmtech.android.bledevice.hrm.model.BleHrRecord10;
import com.cmtech.android.bledevice.hrm.model.RecordWebAsyncTask;
import com.cmtech.android.bledevice.hrm.view.EcgRecordActivity;
import com.cmtech.android.bledevice.hrm.view.HrRecordActivity;
import com.cmtech.android.bledevice.interf.IRecord;
import com.cmtech.android.bledevice.thermo.model.BleThermoRecord10;
import com.cmtech.android.bledevice.thermo.view.ThermoRecordActivity;
import com.cmtech.android.bledevice.thm.model.BleTempHumidRecord10;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.RecordListAdapter;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.vise.log.ViseLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

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

public class RecordExplorerActivity extends AppCompatActivity {
    private static final String TAG = "RecordExplorerActivity";
    private static final int RECORD_TYPE_ECG = 0;
    private static final int RECORD_TYPE_HR = 1;
    private static final int RECORD_TYPE_THM = 2;
    private static final int RECORD_TYPE_THERMO = 3;

    private static long updateTime = new Date().getTime();

    private List<IRecord> allRecords = new ArrayList<>(); // all records
    private RecordListAdapter adapter; // Adapter
    private RecyclerView view; // RecycleView
    private TextView tvPromptInfo; // prompt info

    private int recordType = RECORD_TYPE_ECG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_record_explorer);
        setSupportActionBar(toolbar);

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

        setRecordType(RECORD_TYPE_ECG);

        if(allRecords.size() < 10)
            updateRecords(updateTime);
    }

    private void updateRecords(long fromTime) {
        if(recordType != RECORD_TYPE_ECG) return;

        final BleEcgRecord10 record = BleEcgRecord10.create(new byte[]{0x01,0x00},null, AccountManager.getAccount(), 0,0,0);
        record.setCreateTime(fromTime);

        new RecordWebAsyncTask(this, RecordWebAsyncTask.RECORD_DOWNLOAD_CMD, new RecordWebAsyncTask.RecordWebCallback() {
            @Override
            public void onFinish(Object[] objs) {
                MyApplication.showMessageUsingShortToast((Integer)objs[0]+(String)objs[1]);
                if((Integer) objs[0] == 0) {
                    BleEcgRecord10 newRecord = null;
                    try {
                        JSONArray jsonArr = (JSONArray) objs[2];
                        if(jsonArr.length() == 0) return;
                        for(int i = 0; i < jsonArr.length(); i++) {
                            JSONObject json = (JSONObject) jsonArr.get(i);
                            String devAddress = json.getString("devAddress");
                            long createTime = json.getLong("createTime");
                            Account account = new Account();
                            account.setPlatName(json.getString("creatorPlat"));
                            account.setPlatId(json.getString("creatorId"));
                            int sampleRate = json.getInt("sampleRate");
                            int caliValue = json.getInt("caliValue");
                            int leadTypeCode = json.getInt("leadTypeCode");
                            int recordSecond = json.getInt("recordSecond");
                            String note = json.getString("note");
                            String ecgDataStr = json.getString("ecgData");
                            List<Short> ecgData = new ArrayList<>();
                            String[] strings = ecgDataStr.split(",");
                            for(String str : strings) {
                                ecgData.add(Short.parseShort(str));
                            }

                            newRecord = BleEcgRecord10.create(new byte[]{0x01,0x00}, devAddress, account, sampleRate, caliValue, leadTypeCode);
                            newRecord.setCreateTime(createTime);
                            newRecord.setRecordSecond(recordSecond);
                            newRecord.setNote(note);
                            newRecord.setEcgData(ecgData);
                            ViseLog.e(newRecord);
                            newRecord.saveIfNotExist("createTime = ? and devAddress = ?", ""+newRecord.getCreateTime(), newRecord.getDevAddress());
                        }
                        List<BleEcgRecord10> ecgRecords = LitePal.select("createTime, devAddress, creatorPlat, creatorId, recordSecond")
                                .where("createTime < ? and createTime >= ?", ""+updateTime, ""+newRecord.getCreateTime()).find(BleEcgRecord10.class);
                        allRecords.addAll(ecgRecords);
                        updateTime = newRecord.getCreateTime();
                        updateRecordList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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
                changeRecordType(RECORD_TYPE_ECG);
                break;

            case R.id.hr_record:
                changeRecordType(RECORD_TYPE_HR);
                break;

            case R.id.thm_record:
                changeRecordType(RECORD_TYPE_THM);
                break;

            case R.id.thermo_record:
                changeRecordType(RECORD_TYPE_THERMO);
                break;
        }
        return true;
    }

    public void changeRecordType(int recordType) {
        if(this.recordType == recordType) return;
        setRecordType(recordType);
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
        allRecords.clear();

        switch (recordType) {
            case RECORD_TYPE_ECG:
                List<BleEcgRecord10> ecgRecords = LitePal.select("createTime, devAddress, creatorPlat, creatorId, recordSecond")
                        .where("createTime >= ?", String.valueOf(updateTime)).order("createTime desc").find(BleEcgRecord10.class);
                allRecords.addAll(ecgRecords);
                break;

            case RECORD_TYPE_HR:
                List<BleHrRecord10> hrRecords = LitePal.select("createTime, devAddress, creatorPlat, creatorId, recordSecond").find(BleHrRecord10.class);
                allRecords.addAll(hrRecords);
                break;

            case RECORD_TYPE_THERMO:
                List<BleThermoRecord10> thermoRecords = LitePal.select("createTime, devAddress, creatorPlat, creatorId, highestTemp").find(BleThermoRecord10.class);
                allRecords.addAll(thermoRecords);
                break;

            case RECORD_TYPE_THM:
                List<BleTempHumidRecord10> thmRecords = LitePal.select("createTime, devAddress, creatorPlat, creatorId, temperature, humid, heatIndex, location").find(BleTempHumidRecord10.class);
                allRecords.addAll(thmRecords);
                break;
        }
        updateRecordList();
    }


    public void selectRecord(final IRecord record) {
        Intent intent = null;
        if(record instanceof BleHrRecord10) {
            intent = new Intent(this, HrRecordActivity.class);
        } else if(record instanceof BleEcgRecord10) {
            intent = new Intent(this, EcgRecordActivity.class);
        } else if(record instanceof BleThermoRecord10) {
            intent = new Intent(this, ThermoRecordActivity.class);
        }
        if(intent != null) {
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
                        updateRecordList();
                    }
                    if(record instanceof BleHrRecord10) {
                        LitePal.delete(BleHrRecord10.class, record.getId());
                    } else if(record instanceof BleEcgRecord10) {
                        LitePal.delete(BleEcgRecord10.class, record.getId());
                        new RecordWebAsyncTask(RecordExplorerActivity.this, RecordWebAsyncTask.RECORD_DELETE_CMD, new RecordWebAsyncTask.RecordWebCallback() {
                            @Override
                            public void onFinish(Object[] objs) {
                                MyApplication.showMessageUsingShortToast((Integer)objs[0]+(String)objs[1]);
                            }
                        }).execute((BleEcgRecord10)record);
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

    private void updateRecordList() {
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
