package com.cmtech.android.bledevice.hrm.activityfragment;

import static com.cmtech.android.bledeviceapp.data.record.BleHrRecord.HR_MA_FILTER_SPAN;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordActivity;
import com.cmtech.android.bledeviceapp.data.record.BleHrRecord;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.view.HrHistogramChart;
import com.cmtech.android.bledeviceapp.view.MyLineChart;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

public class HrRecordActivity extends RecordActivity {
    private EditText etMaxHr; // max HR
    private EditText etAveHr; // average HR
    private EditText etHrv; // HRV value
    private EditText etCalories;
    private MyLineChart hrLineChart; // HR line chart
    private HrHistogramChart hrHistChart; // HR histogram chart


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_hr);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);
        record = LitePal.find(BleHrRecord.class, recordId, true);
        ViseLog.e(record);

        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        } else {
            ((BleHrRecord)record).createHistogramFromHrHist();
            initUI();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void initUI() {
        super.initUI();

        hrLineChart = findViewById(R.id.hr_line_chart);
        hrLineChart.setXAxisValueFormatter(HR_MA_FILTER_SPAN);
        hrLineChart.showShortLineChart(((BleHrRecord)record).getHrList(), getResources().getString(R.string.hr_linechart), Color.BLUE);

        TextView tvYUnit = findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.BPM);

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        etAveHr = findViewById(R.id.et_hr_ave_value);
        etMaxHr = findViewById(R.id.et_hr_max_value);
        etHrv = findViewById(R.id.et_hrv);
        etCalories = findViewById(R.id.et_burned_calories);

        etAveHr.setText(String.valueOf(((BleHrRecord)record).getHrAve()));
        etMaxHr.setText(String.valueOf(((BleHrRecord)record).getHrMax()));

        etHrv.setText(String.valueOf(((BleHrRecord)record).calculateHRVInMs()));
        etCalories.setText(String.valueOf(((BleHrRecord)record).calculateCalories(MyApplication.getAccount())));

        hrHistChart.update(((BleHrRecord)record).getHrHistogram());
    }
}
