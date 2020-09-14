package com.cmtech.android.bledevice.thermo.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleThermoRecord10;
import com.cmtech.android.bledevice.view.MyLineChart;
import com.cmtech.android.bledevice.view.RecordIntroLayout;
import com.cmtech.android.bledevice.view.RecordNoteLayout;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebOperationCallback;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.SUCCESS;

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
    private BleThermoRecord10 record;

    private RecordIntroLayout introLayout;

    private MyLineChart lineChart; //

    private RecordNoteLayout noteLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_thermo);

        int recordId = getIntent().getIntExtra("record_id", -1);

        record = LitePal.where("id = ?", ""+recordId).findFirst(BleThermoRecord10.class);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        if(record.getNote() == null) {
            record.setNote("");
            record.save();
        }

        if(record.noSignal()) {
            record.download(this, new IWebOperationCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    if (code == SUCCESS) {
                        initUI();
                    } else {
                        Toast.makeText(ThermoRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            });
        } else {
            initUI();
        }
    }

    private void initUI() {
        ViseLog.e(record);

        introLayout = findViewById(R.id.layout_record_intro);
        introLayout.redraw(record);

        lineChart = findViewById(R.id.line_chart);
        lineChart.setXAxisValueFormatter(2);
        lineChart.showFloatLineChart(record.getTemp(), getResources().getString(R.string.thermo_linechart), Color.BLUE);

        TextView tvYUnit = findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.temperature);

        noteLayout = findViewById(R.id.layout_record_note);
        noteLayout.setRecord(record);
    }
}
