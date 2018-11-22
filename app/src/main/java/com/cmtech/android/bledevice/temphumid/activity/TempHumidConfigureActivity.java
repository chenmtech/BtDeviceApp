package com.cmtech.android.bledevice.temphumid.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.cmtech.android.bledeviceapp.R;

public class TempHumidConfigureActivity extends AppCompatActivity {
    private static final String TAG = "TempHumidConfigureActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temphumid_configure);

        //Bundle bundle = (Bundle) getIntent().getExtras();
        //ViseLog.i("" + device.getMacAddress());
    }
}
