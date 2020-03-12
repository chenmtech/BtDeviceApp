package com.cmtech.android.bledevice.hrmonitor.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;

public class HRMCfgActivity extends AppCompatActivity {
    private boolean ecgLock = true;
    private TextView tvStatus;
    private Button btnSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrm_cfg);

        tvStatus = findViewById(R.id.tv_ecg_switch_status);
        btnSwitch = findViewById(R.id.btn_ecg_switch);

        Intent intent = getIntent();
        if(intent != null) {
            ecgLock = intent.getBooleanExtra("ecg_lock", true);
        }

        if(ecgLock) {
            btnSwitch.setText("解锁心电");
        } else {
            btnSwitch.setText("关闭心电");
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
    }

}
