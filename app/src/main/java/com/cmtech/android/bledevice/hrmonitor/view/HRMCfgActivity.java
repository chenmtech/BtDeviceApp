package com.cmtech.android.bledevice.hrmonitor.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
            btnSwitch.setText("解锁");
            tvStatus.setText("已锁住");
        } else {
            btnSwitch.setText("加锁");
            tvStatus.setText("已解锁");
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
}
