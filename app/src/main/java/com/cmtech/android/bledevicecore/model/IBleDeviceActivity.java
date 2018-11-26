package com.cmtech.android.bledevicecore.model;

public interface IBleDeviceActivity {
    // 关闭Fragment对应的设备，并销毁Fragment
    void closeDevice(BleDeviceFragment fragment);
}
