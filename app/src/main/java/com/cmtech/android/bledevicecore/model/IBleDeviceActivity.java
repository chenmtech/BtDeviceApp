package com.cmtech.android.bledevicecore.model;

public interface IBleDeviceActivity {
    // 由Fragment获取相应的控制器
    BleDeviceController getController(BleDeviceFragment fragment);
    // 关闭Fragment对应的设备，并销毁Fragment
    void closeDevice(BleDeviceFragment fragment);
}
