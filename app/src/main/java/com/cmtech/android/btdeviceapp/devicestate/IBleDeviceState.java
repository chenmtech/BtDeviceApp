package com.cmtech.android.btdeviceapp.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;

public interface IBleDeviceState {
    void open();
    void close();
    void scan();
    void disconnect();
    void switchState();

    void onDeviceScanSuccess();
    void onDeviceScanFailure();
    void onDeviceConnectSuccess(DeviceMirror mirror);
    void onDeviceConnectFailure();
    void onDeviceConnectTimeout();
    void onDeviceDisconnect();

    String getStateDescription();

    boolean canConnect();
    boolean canDisconnect();
    boolean canClose();
}
