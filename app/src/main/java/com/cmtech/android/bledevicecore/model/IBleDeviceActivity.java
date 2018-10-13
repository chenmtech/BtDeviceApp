package com.cmtech.android.bledevicecore.model;

public interface IBleDeviceActivity {
    BleDeviceController getController(BleDeviceFragment fragment);

    void closeDevice(BleDeviceFragment fragment);
}
