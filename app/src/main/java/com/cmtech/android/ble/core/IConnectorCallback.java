package com.cmtech.android.ble.core;

/**
 * 设备连接器的回调接口
 */
public interface IConnectorCallback {
    // 连接成功后的操作
    boolean onConnectSuccess();

    // 连接失败后的操作
    void onConnectFailure();

    // 连接断开后的操作
    void onDisconnect();

    // 连接状态更新后的操作
    void onConnectStateUpdated();
}
