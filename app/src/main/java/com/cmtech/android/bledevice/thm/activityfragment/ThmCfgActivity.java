package com.cmtech.android.bledevice.thm.activityfragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cmtech.android.bledeviceapp.R;

public class ThmCfgActivity extends AppCompatActivity {
    private static final String TAG = "TempHumidConfigureActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cfg_thm);

        //Bundle bundle = (Bundle) getIntent().getExtras();
        //ViseLog.i("" + device.getDeviceAddress());
    }
}
