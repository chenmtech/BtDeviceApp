package com.cmtech.android.bledevice.thermo.activityfragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BleThermoRecord;
import com.cmtech.android.bledeviceapp.view.MyLineChart;
import com.cmtech.android.bledeviceapp.view.layout.RecordIntroductionLayout;
import com.cmtech.android.bledeviceapp.view.layout.RecordNoteLayout;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.thermo.view
 * ClassName:      ThermoRecordActivity
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/3 下午5:35
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/3 下午5:35
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class ThermoRecordActivity extends AppCompatActivity {
    private BleThermoRecord record;

    private RecordIntroductionLayout introLayout;
    private RecordNoteLayout noteLayout;

    private MyLineChart lineChart; //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_thermo);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);
        record = LitePal.find(BleThermoRecord.class, recordId, true);
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
        
        lineChart = findViewById(R.id.line_chart);
        lineChart.setXAxisValueFormatter(2);
        lineChart.showFloatLineChart(record.getTemp(), getResources().getString(R.string.thermo_linechart), Color.BLUE);

        TextView tvYUnit = findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.temperature);
    }
}
