package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDeviceConfig;
import com.cmtech.android.bledeviceapp.R;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;

import static com.cmtech.android.bledevicecore.BleDeviceConstant.IMAGEDIR;

public class EcgMonitorConfigureActivity extends AppCompatActivity {
    private static final String TAG = "EcgMonitorConfigureActivity";

    private Button btnCancel;
    private Button btnOk;
    private Button btnDefault;
    private CheckBox cbIsWarnWhenDisconnect;
    private CheckBox cbIsWarnWhenHrAbnormal;
    private EditText etHrLowLimit;
    private EditText etHrHighLimit;

    private EcgMonitorDeviceConfig config;

    private EcgMonitorDeviceConfig configBackup = new EcgMonitorDeviceConfig();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ecgmonitor_configure);

        Intent intent = getIntent();
        if(intent != null) {
            config = (EcgMonitorDeviceConfig) intent.getSerializableExtra("configuration");
        }

        if(config == null) {
            finish();
        }

        configBackup.setWarnWhenDisconnect(config.isWarnWhenDisconnect());
        configBackup.setWarnWhenHrAbnormal(config.isWarnWhenHrAbnormal());
        configBackup.setHrLowLimit(config.getHrLowLimit());
        configBackup.setHrHighLimit(config.getHrHighLimit());

        // 设置标题为设备地址
        setTitle("配置心电带：" + config.getMacAddress());

        cbIsWarnWhenDisconnect = findViewById(R.id.cb_ecgmonitor_warnwhendisconnect);
        cbIsWarnWhenDisconnect.setChecked(config.isWarnWhenDisconnect());

        cbIsWarnWhenHrAbnormal = findViewById(R.id.cb_ecgmonitor_warnwhenhrabnormal);
        cbIsWarnWhenHrAbnormal.setChecked(config.isWarnWhenHrAbnormal());

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

                config.setWarnWhenDisconnect(cbIsWarnWhenDisconnect.isChecked());
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
                cbIsWarnWhenDisconnect.setChecked(configBackup.isWarnWhenDisconnect());
                cbIsWarnWhenHrAbnormal.setChecked(configBackup.isWarnWhenHrAbnormal());
                etHrLowLimit.setText(String.valueOf(configBackup.getHrLowLimit()));
                etHrHighLimit.setText(String.valueOf(configBackup.getHrHighLimit()));
            }
        });
    }
}
