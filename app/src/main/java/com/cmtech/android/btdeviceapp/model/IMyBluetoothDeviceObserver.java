package com.cmtech.android.btdeviceapp.model;

/**
 * Created by bme on 2018/3/12.
 */

public interface IMyBluetoothDeviceObserver {
    void updateDeviceInfo(MyBluetoothDevice device, int type);
}
