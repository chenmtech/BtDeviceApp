package com.cmtech.android.bledevice.hrm.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleHrRecord10;
import com.cmtech.android.bledevice.view.HrHistogramChart;
import com.cmtech.android.bledevice.view.MyLineChart;
import com.cmtech.android.bledevice.view.RecordIntroductionLayout;
import com.cmtech.android.bledevice.view.RecordNoteLayout;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import static com.cmtech.android.bledevice.record.BleHrRecord10.HR_MOVE_AVERAGE_FILTER_TIME_SPAN;
import static com.cmtech.android.bledevice.record.IRecord.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.SUCCESS;

public class HrRecordActivity extends AppCompatActivity {
    private BleHrRecord10 record;
    private RecordIntroductionLayout introLayout;

    private TextView tvMaxHr; // max HR
    private TextView tvAveHr; // average HR
    private MyLineChart hrLineChart; // HR line chart
    private HrHistogramChart hrHistChart; // HR histogram chart

    private RecordNoteLayout noteLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_hr);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);

        record = LitePal.where("id = ?", String.valueOf(recordId)).findFirst(BleHrRecord10.class, true);
        if(record == null) {
            Toast.makeText(HrRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        if(record.noSignal()) {
            record.download(this, new IWebCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    if (code == SUCCESS) {
                        initUI();
                    } else {
                        Toast.makeText(HrRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            });
        } else {
            initUI();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void initUI() {
        ViseLog.e(record);
        record.createHistogramFromHrHist();

        introLayout = findViewById(R.id.layout_record_intro);
        introLayout.redraw(record);

        hrLineChart = findViewById(R.id.line_chart);
        hrLineChart.setXAxisValueFormatter(HR_MOVE_AVERAGE_FILTER_TIME_SPAN);
        hrLineChart.showShortLineChart(record.getHrList(), getResources().getString(R.string.hr_linechart), Color.BLUE);

        TextView tvYUnit = findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.BPM);

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        tvAveHr = findViewById(R.id.tv_hr_ave_value);
        tvMaxHr = findViewById(R.id.tv_hr_max_value);

        tvAveHr.setText(String.valueOf(record.getHrAve()));
        tvMaxHr.setText(String.valueOf(record.getHrMax()));
        hrHistChart.update(record.getHrHistogram());

        noteLayout = findViewById(R.id.layout_record_note);
        noteLayout.setRecord(record);
    }
}
