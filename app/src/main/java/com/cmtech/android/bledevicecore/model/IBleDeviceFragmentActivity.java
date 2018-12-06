package com.cmtech.android.bledevicecore.model;

public interface IBleDeviceFragmentActivity extends IBleDeviceStateObserver{
    // 可以由设备的mac地址找到BleDevice对象
    BleDevice getDeviceByMac(String mac);
}
