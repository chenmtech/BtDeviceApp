package com.cmtech.android.bledevice.hrmonitor.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecg.activity.EcgMonitorConfigureActivity;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

public class HRMCfgActivity extends AppCompatActivity implements NumberPicker.Formatter, NumberPicker.OnScrollListener, NumberPicker.OnValueChangeListener {
    private boolean ecgLock = true;
    private TextView tvStatus;
    private Button btnSwitch;

    private NumberPicker npHrLow;
    private NumberPicker npHrHigh;

    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrm_cfg);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_hrm_cfg);
        setSupportActionBar(toolbar);

        tvStatus = findViewById(R.id.tv_ecg_switch_status);
        btnSwitch = findViewById(R.id.btn_ecg_switch);

        Intent intent = getIntent();
        if(intent != null) {
            ecgLock = intent.getBooleanExtra("ecg_lock", true);
        }

        if(ecgLock) {
            btnSwitch.setText("开启");
            tvStatus.setText("已关闭");
        } else {
            btnSwitch.setText("关闭");
            tvStatus.setText("已开启");
        }
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("ecg_lock", !ecgLock);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        List<String> hrList = new ArrayList<>();
        for(int i = 30; i <= 200; i+=5) {
            hrList.add(String.valueOf(i));
        }
        npHrLow = findViewById(R.id.np_hr_low);
        npHrLow.setDisplayedValues(hrList.toArray(new String[0]));
        npHrLow.setFormatter(this);
        npHrLow.setOnScrollListener(this);
        npHrLow.setOnValueChangedListener(this);
        npHrLow.setMinValue(0);
        npHrLow.setMaxValue(hrList.size()-1);
        npHrLow.setValue(4);
        npHrHigh = findViewById(R.id.np_hr_high);
        npHrHigh.setDisplayedValues(hrList.toArray(new String[0]));
        npHrHigh.setFormatter(this);
        npHrHigh.setOnScrollListener(this);
        npHrHigh.setOnValueChangedListener(this);
        npHrHigh.setMinValue(0);
        npHrHigh.setMaxValue(hrList.size()-1);
        npHrHigh.setValue(30);

        btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int low = npHrLow.getValue()*5+30;
                int high = npHrHigh.getValue()*5+30;
                if(high <= low) {
                    Toast.makeText(HRMCfgActivity.this, "上限必须大于下限。", Toast.LENGTH_SHORT).show();
                    return;
                }

                ViseLog.e("low:" + low + "high:" + high);

                /*config.setWarnWhenHrAbnormal(cbIsWarnWhenHrAbnormal.isChecked());
                config.setHrLowLimit(lowLimit);
                config.setHrHighLimit(highLimit);*/

                Intent intent = new Intent();
                //intent.putExtra("configuration", config);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;
        }
        return true;
    }

    @Override
    public String format(int value) {
        return String.valueOf(value);
    }


    @Override
    public void onScrollStateChange(NumberPicker view, int scrollState) {

    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }
}
