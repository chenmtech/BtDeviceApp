package com.cmtech.android.btdeviceapp.interfa;

/**
 * DeviceFragment接口，扩展IMyBluetoothDeviceObserver
 * Created by bme on 2018/3/12.
 */

public interface IDeviceFragment extends IMyBluetoothDeviceObserver {
    // 更新设备连接状态
    void updateConnectState();

    // 连接设备
    void connectDevice();

    // 断开设备
    void disconnectDevice();

    // 执行Gatt初始化操作
    void executeGattInitOperation();

    // 关闭Fragment
    void close();
}
