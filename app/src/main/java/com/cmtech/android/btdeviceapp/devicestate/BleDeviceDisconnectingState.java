package com.cmtech.android.btdeviceapp.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceDisconnectingState implements IBleDeviceState {
    BleDevice device;

    public BleDeviceDisconnectingState(BleDevice device) {
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

    }

    @Override
    public void onDeviceScanFailure() {

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
        device.setBluetoothLeDevice(null);
        device.setState(device.getOpenState());
    }

    @Override
    public String deviceGetStateInfo() {
        return "断开中";
    }
}
