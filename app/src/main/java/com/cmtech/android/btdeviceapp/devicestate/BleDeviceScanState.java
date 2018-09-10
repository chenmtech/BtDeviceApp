package com.cmtech.android.btdeviceapp.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceScanState implements IBleDeviceState {
    BleDevice device;

    public BleDeviceScanState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void deviceOpen() {
        ViseLog.i("action wrong");
    }

    @Override
    public void deviceClose() {
        ViseLog.i("action wrong");
    }

    @Override
    public void deviceStartScan() {
        ViseLog.i("action wrong");
    }

    @Override
    public void deviceDisconnect() {
        ViseLog.i("action wrong");
    }

    @Override
    public void onDeviceScanSuccess() {
        device.setState(device.getConnectingState());
    }

    @Override
    public void onDeviceScanFailure() {
        device.setState(device.getOpenState());
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {

    }

    @Override
    public void onDeviceConnectFailure() {

    }

    @Override
    public void onDeviceConnectTimeout() {

    }

    @Override
    public void onDeviceDisconnect() {

    }

    @Override
    public String deviceGetStateInfo() {
        return "扫描中";
    }
}
