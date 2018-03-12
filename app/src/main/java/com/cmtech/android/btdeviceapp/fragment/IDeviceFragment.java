package com.cmtech.android.btdeviceapp.fragment;

import com.cmtech.android.btdeviceapp.model.IMyBluetoothDeviceObserver;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;

/**
 * Created by bme on 2018/3/12.
 */

public interface IDeviceFragment extends IMyBluetoothDeviceObserver {
    // 设备连接状态更新
    void updateConnectState();

    // 连接设备
    void connectDevice();

    // 断开设备
    void disconnectDevice();

    // 连接设备后需要做的Gatt初始化
    void initializeGatt();
}
