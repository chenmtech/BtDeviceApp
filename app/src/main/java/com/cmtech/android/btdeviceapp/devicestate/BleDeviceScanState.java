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
    public void open() {
        ViseLog.i("action wrong");
    }

    @Override
    public void close() {
        ViseLog.i("action wrong");
    }

    @Override
    public void scan() {
        ViseLog.i("action wrong");
    }

    @Override
    public void disconnect() {
        ViseLog.i("action wrong");
    }

    @Override
    public void switchState() {
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
        device.setState(device.getConnectedState());
        device.processConnectSuccess(mirror);
    }

    @Override
    public void onDeviceConnectFailure() {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceConnectTimeout() {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceDisconnect() {
        ViseLog.i("callback wrong");
    }

    @Override
    public String getStateDescription() {
        return "扫描中";
    }

    @Override
    public boolean canConnect() {
        return false;
    }

    @Override
    public boolean canDisconnect() {
        return false;
    }

    @Override
    public boolean canClose() {
        return false;
    }
}
