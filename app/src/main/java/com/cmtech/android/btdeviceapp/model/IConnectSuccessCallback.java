package com.cmtech.android.btdeviceapp.model;

/**
 * Created by bme on 2018/3/14.
 */

public interface IConnectSuccessCallback {
    // 当设备连接成功后需要执行的操作
    void doAfterConnectSuccess(MyBluetoothDevice device);
}
