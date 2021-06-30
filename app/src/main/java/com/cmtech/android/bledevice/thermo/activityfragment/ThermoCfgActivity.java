package com.cmtech.android.bledevice.thermo.activityfragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cmtech.android.bledeviceapp.R;

public class ThermoCfgActivity extends AppCompatActivity {
    private static final String TAG = "ThermoConfigureActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cfg_thermo);

    }
}
