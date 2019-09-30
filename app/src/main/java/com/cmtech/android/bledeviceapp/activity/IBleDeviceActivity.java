package com.cmtech.android.bledeviceapp.activity;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.OnBleDeviceStateListener;

/**
 * IBleDeviceActivity: 包含BleDeviceFragment的Activity必须要实现的接口
 * 该接口必须实现IBleDeviceStateObserver
 * Created by bme on 2018/3/1.
 */

public interface IBleDeviceActivity extends OnBleDeviceStateListener {
    BleDevice findDevice(String mac); // 由设备的mac地址找到BleDevice对象

    void closeFragment(BleFragment fragment); // 关闭Fragment
}
