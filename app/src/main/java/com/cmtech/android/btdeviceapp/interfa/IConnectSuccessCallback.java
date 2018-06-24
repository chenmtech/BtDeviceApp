package com.cmtech.android.btdeviceapp.interfa;

import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;

/**
 * 设备连接成功回调接口
 * Created by bme on 2018/3/14.
 */

public interface IConnectSuccessCallback {
    // 当设备连接成功后需要执行的操作
    void doAfterConnectSuccess(BLEDeviceModel device);
}
