package com.cmtech.android.btdeviceapp.interfa;

import com.cmtech.android.btdeviceapp.model.BleDevice;

/**
 * IBleDeviceStateObserver: 设备状态观察者接口
 * Created by bme on 2018/3/12.
 */

public interface IBleDeviceStateObserver {

    // 更新设备状态
    void updateDeviceState(BleDevice device);
}
