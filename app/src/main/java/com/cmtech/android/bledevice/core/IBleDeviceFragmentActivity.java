package com.cmtech.android.bledevice.core;

/**
 * IBleDeviceFragmentActivity: 包含BleDeviceFragment的Activity必须要实现的接口
 * 该接口必须实现IBleDeviceStateObserver
 * Created by bme on 2018/3/1.
 */

public interface IBleDeviceFragmentActivity extends OnBleDeviceStateListener {
    // 由设备的mac地址找到BleDevice对象
    BleDevice findDevice(String mac);
    void closeFragment(BleDeviceFragment fragment);
}
