package com.cmtech.android.bledevice.hrm.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.cmtech.android.bledevice.hrm.model.HrmCfg;
import com.cmtech.android.bledeviceapp.R;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.hrm.model.HrmCfg.DEFAULT_HR_HIGH_LIMIT;
import static com.cmtech.android.bledevice.hrm.model.HrmCfg.DEFAULT_HR_LOW_LIMIT;

public class HrmCfgActivity extends AppCompatActivity implements NumberPicker.Formatter, NumberPicker.OnScrollListener, NumberPicker.OnValueChangeListener {
    private static final int HR_LIMIT_INTERVAL = 5;
    private HrmCfg hrCfg;

    private NumberPicker npHrLow;
    private NumberPicker npHrHigh;
    private CheckBox cbWarn;

    private EditText etSpeakPeriod;
    private CheckBox cbSpeak;

    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cfg_hrm);

        // create ToolBar
        Toolbar toolbar = findViewById(R.id.tb_hrm_cfg);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if(intent != null) {
            hrCfg = (HrmCfg) intent.getSerializableExtra("hr_cfg");
        }

        List<String> hrLimitValues = new ArrayList<>();
        for(int i = DEFAULT_HR_LOW_LIMIT; i <= DEFAULT_HR_HIGH_LIMIT; i+=HR_LIMIT_INTERVAL) {
            hrLimitValues.add(String.valueOf(i));
        }
        npHrLow = findViewById(R.id.np_hr_low);
        npHrLow.setDisplayedValues(hrLimitValues.toArray(new String[0]));
        npHrLow.setFormatter(this);
        npHrLow.setOnScrollListener(this);
        npHrLow.setOnValueChangedListener(this);
        npHrLow.setMinValue(0);
        npHrLow.setMaxValue(hrLimitValues.size()-1);
        npHrLow.setValue((hrCfg.getHrLow() - DEFAULT_HR_LOW_LIMIT)/HR_LIMIT_INTERVAL);
        npHrHigh = findViewById(R.id.np_hr_high);
        npHrHigh.setDisplayedValues(hrLimitValues.toArray(new String[0]));
        npHrHigh.setFormatter(this);
        npHrHigh.setOnScrollListener(this);
        npHrHigh.setOnValueChangedListener(this);
        npHrHigh.setMinValue(0);
        npHrHigh.setMaxValue(hrLimitValues.size()-1);
        npHrHigh.setValue((hrCfg.getHrHigh() - DEFAULT_HR_LOW_LIMIT)/HR_LIMIT_INTERVAL);

        cbWarn = findViewById(R.id.cb_hr_warn);
        cbWarn.setChecked(hrCfg.needWarn());

        etSpeakPeriod = findViewById(R.id.et_speak_period);
        etSpeakPeriod.setText(String.valueOf(hrCfg.getSpeakPeriod()));
        cbSpeak = findViewById(R.id.cb_hr_speak);
        cbSpeak.setChecked(hrCfg.isSpeak());

        btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int low = npHrLow.getValue()*HR_LIMIT_INTERVAL+DEFAULT_HR_LOW_LIMIT;
                int high = npHrHigh.getValue()*HR_LIMIT_INTERVAL+DEFAULT_HR_LOW_LIMIT;
                if(high <= low) {
                    Toast.makeText(HrmCfgActivity.this, getString(R.string.hr_limit_wrong), Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean isWarn = cbWarn.isChecked();
                int speakPeriod = Integer.parseInt(etSpeakPeriod.getText().toString());
                if(speakPeriod <= 0) {
                    Toast.makeText(HrmCfgActivity.this, "语音播报频率不能小于等于0", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean isSpeak = cbSpeak.isChecked();
                hrCfg.setHrLow(low);
                hrCfg.setHrHigh(high);
                hrCfg.setNeedWarn(isWarn);
                hrCfg.setSpeakPeriod(speakPeriod);
                hrCfg.setSpeak(isSpeak);

                Intent intent = new Intent();
                intent.putExtra("hr_cfg", hrCfg);
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
