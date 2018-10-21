package com.cmtech.android.bledevicecore.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;

/**
 * IBleDeviceState: 设备状态接口
 * Created by bme on 2018/9/12.
 */

public interface IBleDeviceState {
    void open();
    void close();
    void switchState();

    void onDeviceScanFinish(boolean result);
    void onDeviceConnectSuccess(DeviceMirror mirror);
    void onDeviceConnectFailure();
    void onDeviceDisconnect(boolean isActive);

    String getStateDescription();
    boolean canConnect();
    boolean canDisconnect();
    boolean canClose();
}
