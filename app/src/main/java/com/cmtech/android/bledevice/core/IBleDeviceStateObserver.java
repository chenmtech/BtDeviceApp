package com.cmtech.android.bledevice.core;

/**
 * IBleDeviceStateObserver: 设备状态观察者接口
 * Created by bme on 2018/3/12.
 */

public interface IBleDeviceStateObserver {
    // 更新设备状态
    void updateDeviceState(final BleDevice device);

    /**
     * 通知设备重连失败，更新报警状态
     * @param device
     * @param warn: true-警告，false-消除警告
     */
    void notifyReconnectFailure(final BleDevice device, boolean warn);

}
