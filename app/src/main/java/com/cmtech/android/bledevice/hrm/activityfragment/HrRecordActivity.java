package com.cmtech.android.bledevice.hrm.activityfragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BleHrRecord10;
import com.cmtech.android.bledeviceapp.util.MathUtil;
import com.cmtech.android.bledeviceapp.view.HrHistogramChart;
import com.cmtech.android.bledeviceapp.view.MyLineChart;
import com.cmtech.android.bledeviceapp.view.layout.RecordIntroductionLayout;
import com.cmtech.android.bledeviceapp.view.layout.RecordNoteLayout;

import org.litepal.LitePal;

import java.util.List;

import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.INVALID_ID;
import static com.cmtech.android.bledeviceapp.data.record.BleHrRecord10.HR_MA_FILTER_SPAN;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class HrRecordActivity extends AppCompatActivity {
    private BleHrRecord10 record;
    private RecordIntroductionLayout introLayout;
    private RecordNoteLayout noteLayout;

    private TextView tvMaxHr; // max HR
    private TextView tvAveHr; // average HR
    private TextView tvHrv; // HRV value
    private MyLineChart hrLineChart; // HR line chart
    private HrHistogramChart hrHistChart; // HR histogram chart


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_hr);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);
        record = LitePal.find(BleHrRecord10.class, recordId, true);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        if(record.noSignal()) {
            record.download(this, code -> {
                if (code == RETURN_CODE_SUCCESS) {
                    initUI();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
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

        hrLineChart = findViewById(R.id.line_chart);
        hrLineChart.setXAxisValueFormatter(HR_MA_FILTER_SPAN);
        hrLineChart.showShortLineChart(record.getHrList(), getResources().getString(R.string.hr_linechart), Color.BLUE);

        TextView tvYUnit = findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.BPM);

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        tvAveHr = findViewById(R.id.tv_hr_ave_value);
        tvMaxHr = findViewById(R.id.tv_hr_max_value);
        tvHrv = findViewById(R.id.tv_hrv);

        tvAveHr.setText(String.valueOf(record.getHrAve()));
        tvMaxHr.setText(String.valueOf(record.getHrMax()));

        List<Short> hrListMs = record.getHrListInMS();
        tvHrv.setText(String.valueOf((short)MathUtil.shortStd(hrListMs)));

        hrHistChart.update(record.getHrHistogram());
    }
}
