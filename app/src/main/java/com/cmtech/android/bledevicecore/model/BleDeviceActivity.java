package com.cmtech.android.bledevicecore.model;

import android.support.v7.app.AppCompatActivity;

public abstract class BleDeviceActivity extends AppCompatActivity {
    // 由Fragment获取相应的控制器
    public abstract BleDeviceController getController(BleDeviceFragment fragment);
    // 关闭Fragment对应的设备，并销毁Fragment
    public abstract void closeDevice(BleDeviceFragment fragment);
}
