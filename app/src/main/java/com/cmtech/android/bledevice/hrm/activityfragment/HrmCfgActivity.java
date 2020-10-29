package com.cmtech.android.bledevice.hrm.activityfragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

    private CheckBox cbSpeak;
    private NumberPicker npSpeechFrequency;

    private CheckBox cbWarn;
    private NumberPicker npHrLow;
    private NumberPicker npHrHigh;

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

        cbSpeak = findViewById(R.id.cb_hr_speak);
        cbSpeak.setChecked(hrCfg.isSpeak());

        npSpeechFrequency = findViewById(R.id.np_speech_frequency);
        npSpeechFrequency.setMaxValue(10);
        npSpeechFrequency.setMinValue(1);
        npSpeechFrequency.setValue(hrCfg.getSpeakPeriod());

        cbWarn = findViewById(R.id.cb_hr_warn);
        cbWarn.setChecked(hrCfg.needWarn());

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

        btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSpeak = cbSpeak.isChecked();
                int speakPeriod = npSpeechFrequency.getValue();

                boolean isWarn = cbWarn.isChecked();
                int low = npHrLow.getValue() * HR_LIMIT_INTERVAL + DEFAULT_HR_LOW_LIMIT;
                int high = npHrHigh.getValue() * HR_LIMIT_INTERVAL + DEFAULT_HR_LOW_LIMIT;
                if(high <= low) {
                    Toast.makeText(HrmCfgActivity.this, getString(R.string.hr_limit_wrong), Toast.LENGTH_SHORT).show();
                    return;
                }

                hrCfg.setSpeak(isSpeak);
                hrCfg.setSpeakPeriod(speakPeriod);
                hrCfg.setNeedWarn(isWarn);
                hrCfg.setHrLow(low);
                hrCfg.setHrHigh(high);

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
