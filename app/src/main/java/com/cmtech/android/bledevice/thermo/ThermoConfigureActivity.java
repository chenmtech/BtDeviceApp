package com.cmtech.android.bledevice.thermo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.cmtech.android.bledeviceapp.R;

public class ThermoConfigureActivity extends AppCompatActivity {
    private static final String TAG = "ThermoConfigureActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermometer_configure);

    }
}
