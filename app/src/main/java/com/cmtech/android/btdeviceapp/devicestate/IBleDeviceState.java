package com.cmtech.android.btdeviceapp.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;

public interface IBleDeviceState {
    void deviceOpen();
    void deviceClose();
    void deviceStartScan();
    void deviceDisconnect();

    void onDeviceScanSuccess();
    void onDeviceScanFailure();
    void onDeviceConnectSuccess(DeviceMirror mirror);
    void onDeviceConnectFailure();
    void onDeviceConnectTimeout();
    void onDeviceDisconnect();

    String deviceGetStateInfo();
}
