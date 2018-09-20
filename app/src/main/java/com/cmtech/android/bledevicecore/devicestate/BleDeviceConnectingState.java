package com.cmtech.android.bledevicecore.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceConnectingState implements IBleDeviceState {
    private BleDevice device;

    public BleDeviceConnectingState(BleDevice device) {
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
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceScanFailure() {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        device.setState(device.getConnectedState());
        device.processConnectSuccess(mirror);
    }

    @Override
    public void onDeviceConnectFailure() {
        device.setState(device.getOpenState());
        device.processConnectFailure();
    }

    @Override
    public void onDeviceConnectTimeout() {
        device.setState(device.getOpenState());
        device.processConnectFailure();
    }

    @Override
    public void onDeviceDisconnect() {
        ViseLog.i("callback wrong");
    }

    @Override
    public String getStateDescription() {
        return "连接中";
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
