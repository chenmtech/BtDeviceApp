package com.cmtech.android.bledevice.hrm.activityfragment;

import static com.cmtech.android.bledevice.hrm.model.HrmCfg.DEFAULT_HR_HIGH_LIMIT;
import static com.cmtech.android.bledevice.hrm.model.HrmCfg.DEFAULT_HR_LOW_LIMIT;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cmtech.android.bledevice.hrm.model.HrmCfg;
import com.cmtech.android.bledeviceapp.R;

import java.util.ArrayList;
import java.util.List;

public class HrmCfgActivity extends AppCompatActivity implements NumberPicker.Formatter, NumberPicker.OnScrollListener, NumberPicker.OnValueChangeListener {
    private static final int HR_LIMIT_INTERVAL = 5;
    private HrmCfg hrmCfg;

    private CheckBox cbSpeak;
    private NumberPicker npSpeechFrequency;

    private CheckBox cbWarn;
    private NumberPicker npHrLow;
    private NumberPicker npHrHigh;

    private CheckBox cbAfib;
    private CheckBox cbSb;

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
            hrmCfg = (HrmCfg) intent.getSerializableExtra("hrm_cfg");
        }

        cbSpeak = findViewById(R.id.cb_hr_speak);
        cbSpeak.setChecked(hrmCfg.isSpeak());

        npSpeechFrequency = findViewById(R.id.np_speech_frequency);
        npSpeechFrequency.setMaxValue(10);
        npSpeechFrequency.setMinValue(1);
        npSpeechFrequency.setValue(hrmCfg.getSpeakPeriod());

        cbWarn = findViewById(R.id.cb_hr_warn);
        cbWarn.setChecked(hrmCfg.needWarn());

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
        npHrLow.setValue((hrmCfg.getHrLow() - DEFAULT_HR_LOW_LIMIT)/HR_LIMIT_INTERVAL);
        npHrHigh = findViewById(R.id.np_hr_high);
        npHrHigh.setDisplayedValues(hrLimitValues.toArray(new String[0]));
        npHrHigh.setFormatter(this);
        npHrHigh.setOnScrollListener(this);
        npHrHigh.setOnValueChangedListener(this);
        npHrHigh.setMinValue(0);
        npHrHigh.setMaxValue(hrLimitValues.size()-1);
        npHrHigh.setValue((hrmCfg.getHrHigh() - DEFAULT_HR_LOW_LIMIT)/HR_LIMIT_INTERVAL);

        cbAfib = findViewById(R.id.cb_rhythm_afib);
        cbAfib.setChecked(hrmCfg.isWarnAfib());
        cbSb = findViewById(R.id.cb_rhythm_sb);
        cbSb.setChecked(hrmCfg.isWarnSb());

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

                hrmCfg.setSpeak(isSpeak);
                hrmCfg.setSpeakPeriod(speakPeriod);
                hrmCfg.setNeedWarn(isWarn);
                hrmCfg.setHrLow(low);
                hrmCfg.setHrHigh(high);

                hrmCfg.setWarnAfib(cbAfib.isChecked());
                hrmCfg.setWarnSb(cbSb.isChecked());

                Intent intent = new Intent();
                intent.putExtra("hr_cfg", hrmCfg);
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
