package com.cmtech.android.bledevice.hrmonitor.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrmonitor.model.BleHrRecord10;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

public class HrRecordActivity extends AppCompatActivity {
    private BleHrRecord10 record;
    private TextView tvCreateTime; // 创建时间
    private TextView tvCreator; // 创建人
    private TextView tvHrNum; // 心率次数

    private TextView tvMaxHr; // 最大心率
    private TextView tvAveHr; // 平均心率
    private HrLineChart hrLineChart; // 心率折线图
    private HrHistogramChart hrHistChart; // 心率直方图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_record);

        int recordId = getIntent().getIntExtra("record_id", -1);
        record = LitePal.find(BleHrRecord10.class, recordId, true);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        ViseLog.e(record);
        record.createHistogram();

        tvCreateTime = findViewById(R.id.tv_create_time);
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        tvCreator = findViewById(R.id.tv_creator);
        tvCreator.setText(record.getCreatorName());

        tvHrNum = findViewById(R.id.tv_hr_num);
        if(record.getFilterHrList() == null)
            tvHrNum.setText(String.valueOf(0));
        else
            tvHrNum.setText(String.valueOf(record.getFilterHrList().size()));

        hrLineChart = findViewById(R.id.hr_line_chart);
        hrLineChart.showLineChart(record.getFilterHrList(), "心率变化", Color.BLUE);

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        tvAveHr = findViewById(R.id.tv_hr_ave_value);
        tvMaxHr = findViewById(R.id.tv_hr_max_value);

        tvAveHr.setText(String.valueOf(record.getHrAve()));
        tvMaxHr.setText(String.valueOf(record.getHrMax()));
        hrHistChart.update(record.getHrHistogram());
    }
}
