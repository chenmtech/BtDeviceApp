package com.cmtech.android.bledevice.thermo.activityfragment;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordActivity;
import com.cmtech.android.bledeviceapp.data.record.BlePttRecord;
import com.cmtech.android.bledeviceapp.data.record.BleThermoRecord;
import com.cmtech.android.bledeviceapp.view.MyLineChart;
import com.cmtech.android.bledeviceapp.view.layout.RecordIntroductionLayout;
import com.cmtech.android.bledeviceapp.view.layout.RecordNoteLayout;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

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
public class ThermoRecordActivity extends RecordActivity {
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
        } else {
            initUI();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void initUI() {
        super.initUI();
        
        lineChart = findViewById(R.id.hr_line_chart);
        lineChart.setXAxisValueFormatter(2);
        lineChart.showFloatLineChart(((BleThermoRecord)record).getTemp(), getResources().getString(R.string.thermo_linechart), Color.BLUE);

        TextView tvYUnit = findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.temperature);
    }
}
