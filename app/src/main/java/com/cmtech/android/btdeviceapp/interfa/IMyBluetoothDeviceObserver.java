package com.cmtech.android.btdeviceapp.interfa;

import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;

/**
 * MyBluetoothDevice的观察者
 * Created by bme on 2018/3/12.
 */

public interface IMyBluetoothDeviceObserver {
    int TYPE_MODIFY_CONNECTSTATE = 0;       // 连接状态改变
    int TYPE_MODIFY_NICKNAME = 1;           // 昵称改变
    int TYPE_MODIFY_AUTOCONNECT = 2;        // 自动连接设置改变
    int TYPE_ADDED = 3;                     // 加入
    int TYPE_DELETED = 4;                   // 删除

    // 更新设备信息
    void updateDeviceInfo(BLEDeviceModel device, int type);
}
