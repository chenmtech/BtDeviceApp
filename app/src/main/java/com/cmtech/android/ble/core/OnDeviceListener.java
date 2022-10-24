package com.cmtech.android.ble.core;

/**
 * 设备监听器接口
 */
public interface OnDeviceListener {
    // 设备连接状态更新了
    void onConnectStateUpdated(final IDevice device);

    // 设备电池电量更新了
    void onBatteryLevelUpdated(final IDevice device);

    // 设备的通知信息更新了
    void onNotificationInfoUpdated(final IDevice device);
}
