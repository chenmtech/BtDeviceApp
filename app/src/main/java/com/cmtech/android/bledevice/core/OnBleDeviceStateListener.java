package com.cmtech.android.bledevice.core;

/**
 * OnBleDeviceStateListener: 设备状态观察者接口
 * Created by bme on 2018/3/12.
 */

public interface OnBleDeviceStateListener {
    void onUpdateDeviceConnectState(final BleDevice device);
    void onNotifyReconnectFailure(final BleDevice device, boolean warn);
    void onUpdateDeviceBattery(final BleDevice device);

}
