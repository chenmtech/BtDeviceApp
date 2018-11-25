package com.cmtech.android.bledevicecore.model;

public interface IBleDeviceFragmentContainer {
    // 必须有能力关闭Fragment对应的设备，并销毁Fragment
    void closeDevice(BleDeviceFragment fragment);
}
