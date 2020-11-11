package com.cmtech.android.bledevice.hrm.activityfragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BleHrRecord;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.view.HrHistogramChart;
import com.cmtech.android.bledeviceapp.view.MyLineChart;
import com.cmtech.android.bledeviceapp.view.layout.RecordIntroductionLayout;
import com.cmtech.android.bledeviceapp.view.layout.RecordNoteLayout;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.data.record.BleHrRecord.HR_MA_FILTER_SPAN;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

public class HrRecordActivity extends AppCompatActivity {
    private BleHrRecord record;
    private RecordIntroductionLayout introLayout;
    private RecordNoteLayout noteLayout;

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
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        } else {
            record.createHistogramFromHrHist();
            initUI();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    private void initUI() {
        introLayout = findViewById(R.id.layout_record_intro);
        introLayout.setRecord(record);
        introLayout.updateView();

        noteLayout = findViewById(R.id.layout_record_note);
        noteLayout.setRecord(record);
        noteLayout.updateView();

        hrLineChart = findViewById(R.id.hr_line_chart);
        hrLineChart.setXAxisValueFormatter(HR_MA_FILTER_SPAN);
        hrLineChart.showShortLineChart(record.getHrList(), getResources().getString(R.string.hr_linechart), Color.BLUE);

        TextView tvYUnit = findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.BPM);

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        etAveHr = findViewById(R.id.et_hr_ave_value);
        etMaxHr = findViewById(R.id.et_hr_max_value);
        etHrv = findViewById(R.id.et_hrv);
        etCalories = findViewById(R.id.et_burned_calories);

        etAveHr.setText(String.valueOf(record.getHrAve()));
        etMaxHr.setText(String.valueOf(record.getHrMax()));

        etHrv.setText(String.valueOf(record.calculateHRVInMs()));
        Account account = MyApplication.getAccount();
        if(account == null || account.notSetPersonInfo())
            etCalories.setText("请完善个人信息");
        else
            etCalories.setText(String.valueOf(record.calculateCalories(account)));

        hrHistChart.update(record.getHrHistogram());
    }
}
