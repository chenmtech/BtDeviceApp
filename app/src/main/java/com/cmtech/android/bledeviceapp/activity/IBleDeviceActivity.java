package com.cmtech.android.bledeviceapp.activity;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.OnBleDeviceUpdatedListener;

/**
 * IBleDeviceActivity: 包含BleDeviceFragment的Activity必须要实现的接口
 * 该接口必须实现IBleDeviceStateObserver
 * Created by bme on 2018/3/1.
 */

public interface IBleDeviceActivity extends OnBleDeviceUpdatedListener {
    //void closeFragment(BleFragment fragment); // 关闭Fragment
}
