package com.cmtech.android.bledevice.hrm.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleHrRecord10;
import com.cmtech.android.bledevice.record.RecordWebAsyncTask;
import com.cmtech.android.bledevice.view.HrHistogramChart;
import com.cmtech.android.bledevice.view.MyLineChart;
import com.cmtech.android.bledevice.view.RecordIntroLayout;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import org.json.JSONObject;
import org.litepal.LitePal;

import static com.cmtech.android.bledevice.record.BleHrRecord10.HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.RECORD_DOWNLOAD_CMD;

public class HrRecordActivity extends AppCompatActivity {
    private static final int INVALID_ID = -1;

    private BleHrRecord10 record;
    private RecordIntroLayout introLayout;

    private TextView tvMaxHr; // 最大心率
    private TextView tvAveHr; // 平均心率
    private MyLineChart lineChart; // 心率折线图
    private HrHistogramChart hrHistChart; // 心率直方图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_hr);// 创建ToolBar

        int recordId = getIntent().getIntExtra("record_id", -1);

        record = LitePal.where("id = ?", ""+recordId).findFirst(BleHrRecord10.class, true);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        if(record.isDataEmpty()) {
            new RecordWebAsyncTask(this, RECORD_DOWNLOAD_CMD, new RecordWebAsyncTask.RecordWebCallback() {
                @Override
                public void onFinish(Object[] objs) {
                    if ((Integer) objs[0] == 0) {
                        JSONObject json = (JSONObject) objs[2];

                        if(record.setDataFromJson(json)) {
                            initUI();
                            return;
                        }
                    }
                    Toast.makeText(HrRecordActivity.this, "获取记录数据失败，无法打开记录", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }).execute(record);
        } else {
            initUI();
        }
    }

    private void initUI() {
        ViseLog.e(record);
        record.createHistogram();

        introLayout = findViewById(R.id.layout_record_intro);
        introLayout.redraw(record, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });

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
