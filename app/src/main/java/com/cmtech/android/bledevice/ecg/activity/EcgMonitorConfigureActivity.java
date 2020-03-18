package com.cmtech.android.bledevice.ecg.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecg.device.EcgConfiguration;
import com.cmtech.android.bledeviceapp.R;

import static com.cmtech.android.bledevice.ecg.EcgConstant.DEFAULT_HR_HIGH_LIMIT;
import static com.cmtech.android.bledevice.ecg.EcgConstant.DEFAULT_HR_LOW_LIMIT;
import static com.cmtech.android.bledevice.ecg.EcgConstant.DEFAULT_WARN_WHEN_HR_ABNORMAL;

public class EcgMonitorConfigureActivity extends AppCompatActivity {
    private static final String TAG = "EcgMonitorConfigureActivity";

    private Button btnCancel;
    private Button btnOk;
    private Button btnDefault;
    private CheckBox cbIsWarnWhenHrAbnormal;
    private EditText etHrLowLimit;
    private EditText etHrHighLimit;

    private EcgConfiguration config;
    private String deviceNickName;
    private final EcgConfiguration configBackup = new EcgConfiguration();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ecgmonitor_configure);

        Intent intent = getIntent();
        if(intent != null) {
            config = (EcgConfiguration) intent.getSerializableExtra("device_configuration");
            deviceNickName = intent.getStringExtra("device_name");
        }

        if(config == null) {
            throw new IllegalStateException("The configure of device is null.");
        }

        configBackup.setWarnWhenHrAbnormal(config.warnWhenHrAbnormal());
        configBackup.setHrLowLimit(config.getHrLowLimit());
        configBackup.setHrHighLimit(config.getHrHighLimit());

        // 设置标题
        setTitle(deviceNickName + "：" + config.getMacAddress());

        cbIsWarnWhenHrAbnormal = findViewById(R.id.cb_hr_warn);
        cbIsWarnWhenHrAbnormal.setChecked(config.warnWhenHrAbnormal());

        etHrLowLimit = findViewById(R.id.et_hr_low_limit);
        etHrLowLimit.setText(String.valueOf(config.getHrLowLimit()));

        etHrHighLimit = findViewById(R.id.et_hr_high_limit);
        etHrHighLimit.setText(String.valueOf(config.getHrHighLimit()));

        btnCancel = findViewById(R.id.btn_ecgmonitor_config_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        btnOk = findViewById(R.id.btn_ecgmonitor_config_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int lowLimit = Integer.parseInt(etHrLowLimit.getText().toString());
                int highLimit = Integer.parseInt(etHrHighLimit.getText().toString());
                if(highLimit <= lowLimit) {
                    Toast.makeText(EcgMonitorConfigureActivity.this, "心率值异常上限必须大于下限。", Toast.LENGTH_SHORT).show();
                    return;
                }

                config.setWarnWhenHrAbnormal(cbIsWarnWhenHrAbnormal.isChecked());
                config.setHrLowLimit(lowLimit);
                config.setHrHighLimit(highLimit);

                Intent intent = new Intent();
                intent.putExtra("configuration", config);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        btnDefault = findViewById(R.id.btn_ecgmonitor_config_default);
        btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cbIsWarnWhenHrAbnormal.setChecked(DEFAULT_WARN_WHEN_HR_ABNORMAL);
                etHrLowLimit.setText(String.valueOf(DEFAULT_HR_LOW_LIMIT));
                etHrHighLimit.setText(String.valueOf(DEFAULT_HR_HIGH_LIMIT));
            }
        });
    }
}
