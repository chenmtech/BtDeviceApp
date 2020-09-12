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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BasicRecord;
import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledevice.record.RecordFactory;
import com.cmtech.android.bledevice.record.RecordType;
import com.cmtech.android.bledevice.record.RecordWebAsyncTask;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.RecordListAdapter;
import com.cmtech.android.bledeviceapp.interfac.IWebOperationCallback;
import com.vise.log.ViseLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cmtech.android.bledeviceapp.AppConstant.SUPPORT_RECORD_TYPES;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.SUCCESS;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_SUCCESS;

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
    private static final int DOWNLOAD_RECORD_BASIC_INFO_NUM = 20;

    private List<IRecord> allRecords = new ArrayList<>(); // all records
    private RecordListAdapter recordAdapter; // Adapter
    private RecyclerView recordView; // RecycleView
    private TextView tvNoRecord; // no record
    private Spinner typeSpinner;
    private RecordType recordType = null; // record type in record list
    private String noteFilterStr = ""; // record note filter string
    private long updateTime; // update time in record list
    private EditText etNoteFilter;

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
        typeSpinner = findViewById(R.id.spinner_record_type);
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
                    updateRecordList(updateTime);
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

        tvNoRecord = findViewById(R.id.tv_no_record);
        tvNoRecord.setText(R.string.no_record);

        etNoteFilter = findViewById(R.id.et_note_filter_string);
        etNoteFilter.setVisibility(View.GONE);
        etNoteFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                setNoteFilterStr(etNoteFilter.getText().toString().trim());
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.search_record:
                if(etNoteFilter.getVisibility() == View.VISIBLE)
                    etNoteFilter.setVisibility(View.GONE);
                else
                    etNoteFilter.setVisibility(View.VISIBLE);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            recordAdapter.notifySelectedItemChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ViseLog.e("RecordExplorerActivity onDestroy");
    }

    private void setRecordType(final RecordType type) {
        if(this.recordType == type) return;
        this.recordType = type;

        allRecords.clear();
        recordAdapter.unselected();
        updateRecordView();

        updateTime = new Date().getTime();
        updateRecordList(updateTime);
    }

    private void setNoteFilterStr(String noteFilterStr) {
        if(this.noteFilterStr.equalsIgnoreCase(noteFilterStr)) return;
        this.noteFilterStr = noteFilterStr;

        allRecords.clear();
        recordAdapter.unselected();
        updateRecordView();

        updateTime = new Date().getTime();
        updateRecordList(updateTime);
    }

    private void updateRecordList(final long from) {
        IRecord record = RecordFactory.create(recordType, from, null, MyApplication.getAccount(), "");
        if(record == null) {
            ViseLog.e("The record type is not supported.");
            return;
        }

        record.query(this, from, noteFilterStr, DOWNLOAD_RECORD_BASIC_INFO_NUM, new IWebOperationCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if(code == SUCCESS) {
                    if(result == null) {
                        Toast.makeText(RecordExplorerActivity.this, R.string.no_more, Toast.LENGTH_SHORT).show();
                    } else {
                        List<? extends IRecord> records = (List<? extends IRecord>) result;
                        updateTime = records.get(records.size() - 1).getCreateTime();
                        allRecords.addAll(records);
                        ViseLog.e(allRecords.toString());
                        updateRecordView();
                    }
                } else {
                    Toast.makeText(RecordExplorerActivity.this, R.string.web_failure, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void openRecord(IRecord record) {
        if(record != null) {
            Intent intent = null;
            Class<? extends Activity> actClass = RecordType.fromCode(record.getTypeCode()).getActivityClass();
            if (actClass != null) {
                intent = new Intent(RecordExplorerActivity.this, actClass);
            }
            if (intent != null) {
                intent.putExtra("record_id", record.getId());
                startActivityForResult(intent, 1);
            }
        }
    }

    public void deleteRecord(final IRecord record) {
        if(record != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delete_record).setMessage(R.string.really_wanna_delete_record);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    record.delete(RecordExplorerActivity.this, new IWebOperationCallback() {
                        @Override
                        public void onFinish(int code, Object result) {
                            if(allRecords.remove(record)) {
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
        record.upload(this, new IWebOperationCallback() {
            @Override
            public void onFinish(int code, final Object rlt) {
                Toast.makeText(RecordExplorerActivity.this, (String) rlt, Toast.LENGTH_SHORT).show();
                if (code == SUCCESS) {
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
