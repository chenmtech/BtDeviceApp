package com.cmtech.android.btdeviceapp.model;

/**
 * Created by bme on 2018/3/12.
 */

public interface IMyBluetoothDeviceObserver {
    int TYPE_MODIFY_CONNECTSTATE = 0;
    int TYPE_MODIFY_NICKNAME = 1;
    int TYPE_MODIFY_AUTOCONNECT = 2;
    int TYPE_ADD_DEVICE = 3;
    int TYPE_DELETE_DEVICE = 4;

    void updateDeviceInfo(MyBluetoothDevice device, int type);
}
