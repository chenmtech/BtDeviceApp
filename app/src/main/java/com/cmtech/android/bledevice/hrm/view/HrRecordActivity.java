package com.cmtech.android.bledevice.hrm.view;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrm.model.BleHrRecord10;
import com.cmtech.android.bledevice.hrm.model.RecordWebAsyncTask;
import com.cmtech.android.bledevice.view.HrHistogramChart;
import com.cmtech.android.bledevice.view.MyLineChart;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.json.JSONObject;
import org.litepal.LitePal;

import static com.cmtech.android.bledevice.hrm.model.BleHrRecord10.HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH;
import static com.cmtech.android.bledevice.hrm.model.RecordWebAsyncTask.RECORD_DOWNLOAD_CMD;
import static com.cmtech.android.bledeviceapp.AppConstant.SUPPORT_LOGIN_PLATFORM;

public class HrRecordActivity extends AppCompatActivity {
    private static final int INVALID_ID = -1;

    private BleHrRecord10 record;
    private TextView tvCreateTime; // 创建时间
    private TextView tvCreator; // 创建人
    private TextView tvAddress;
    private TextView tvTimeLength; // time length
    private ImageView ivRecordType; // record type

    private TextView tvMaxHr; // 最大心率
    private TextView tvAveHr; // 平均心率
    private MyLineChart lineChart; // 心率折线图
    private HrHistogramChart hrHistChart; // 心率直方图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_hr);// 创建ToolBar

        Toolbar toolbar = findViewById(R.id.tb_hr_record);
        setSupportActionBar(toolbar);

        int recordId = getIntent().getIntExtra("record_id", -1);

        record = LitePal.where("id = ?", ""+recordId).findFirst(BleHrRecord10.class, true);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        if(!record.hasData()) {
            new RecordWebAsyncTask(this, RECORD_DOWNLOAD_CMD, new RecordWebAsyncTask.RecordWebCallback() {
                @Override
                public void onFinish(Object[] objs) {
                    if ((Integer) objs[0] == 0) {
                        JSONObject json = (JSONObject) objs[2];

                        if(record.getDataFromJson(json)) {
                            initUI();
                        }
                    }
                }
            }).execute(record);
        } else {
            initUI();
        }
    }

    private void initUI() {
        ViseLog.e(record);
        record.createHistogram();

        ivRecordType = findViewById(R.id.iv_record_type);
        ivRecordType.setImageResource(R.mipmap.ic_hr_24px);

        tvCreateTime = findViewById(R.id.tv_create_time);
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        tvCreator = findViewById(R.id.tv_creator);
        tvCreator.setText(record.getCreatorName());

        Drawable drawable = ContextCompat.getDrawable(this, SUPPORT_LOGIN_PLATFORM.get(record.getCreatorPlat()));
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        tvCreator.setCompoundDrawables(null, drawable, null, null);

        tvTimeLength = findViewById(R.id.tv_desc);
        tvTimeLength.setText(record.getDesc());

        tvAddress = findViewById(R.id.tv_device_address);
        tvAddress.setText(record.getDevAddress());

        lineChart = findViewById(R.id.hr_line_chart);
        lineChart.setXAxisValueFormatter(HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH);
        lineChart.showShortLineChart(record.getFilterHrList(), "心率变化", Color.BLUE);

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        tvAveHr = findViewById(R.id.tv_hr_ave_value);
        tvMaxHr = findViewById(R.id.tv_hr_max_value);

        tvAveHr.setText(String.valueOf(record.getHrAve()));
        tvMaxHr.setText(String.valueOf(record.getHrMax()));
        hrHistChart.update(record.getHrHistogram());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.ecg_upload:
                upload();
                break;
        }
        return true;
    }

    private void upload() {
        new RecordWebAsyncTask(this, RecordWebAsyncTask.RECORD_QUERY_CMD, new RecordWebAsyncTask.RecordWebCallback() {
            @Override
            public void onFinish(final Object[] objs) {
                final boolean result = ((Integer)objs[0] == 0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result) {
                            int id = (Integer) objs[2];
                            if(id == INVALID_ID) {
                                new RecordWebAsyncTask(HrRecordActivity.this, RecordWebAsyncTask.RECORD_UPLOAD_CMD, new RecordWebAsyncTask.RecordWebCallback() {
                                    @Override
                                    public void onFinish(Object[] objs) {
                                        MyApplication.showMessageUsingShortToast((Integer)objs[0]+(String)objs[1]);
                                    }
                                }).execute(record);
                            }
                        } else {
                            MyApplication.showMessageUsingShortToast((String)objs[1]);
                        }
                    }
                });
            }
        }).execute(record);
    }
}
