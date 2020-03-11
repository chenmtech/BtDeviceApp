package com.cmtech.android.bledevice.hrmonitor.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecg.activity.EcgMonitorConfigureActivity;
import com.cmtech.android.bledevice.ecg.device.EcgConfiguration;
import com.cmtech.android.bledeviceapp.R;

public class HRMCfgActivity extends AppCompatActivity {
    private boolean ecgSwitchOn = false;
    private TextView tvStatus;
    private Button btnSwitch;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrm_cfg);

        tvStatus = findViewById(R.id.tv_ecg_switch_status);
        btnSwitch = findViewById(R.id.btn_ecg_switch);
        btnCancel = findViewById(R.id.btn_cancel);

        Intent intent = getIntent();
        if(intent != null) {
            ecgSwitchOn = intent.getBooleanExtra("ecg_on", false);
        }

        if(ecgSwitchOn) {
            tvStatus.setText("当前为心电模式");
            btnSwitch.setText("转换为心率模式");
        } else {
            tvStatus.setText("当前为心率模式");
            btnSwitch.setText("转换为心电模式");
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("ecg_on", !ecgSwitchOn);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

}
