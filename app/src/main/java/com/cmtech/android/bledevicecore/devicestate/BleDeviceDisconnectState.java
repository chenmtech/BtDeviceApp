package com.cmtech.android.bledevicecore.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceDisconnectState implements IBleDeviceState {
    private BleDevice device;

    public BleDeviceDisconnectState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void open() {
        ViseLog.e(this + " : action wrong");
    }

    @Override
    public void close() {
        device.setState(device.getCloseState());
    }

    @Override
    public void switchState() {
        device.startScan();
    }

    @Override
    public void onDeviceScanFinish(boolean result) {
        ViseLog.e(this + " : callback wrong");
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        device.onStateConnectSuccess(mirror);
    }

    @Override
    public void onDeviceConnectFailure() {
        device.onStateConnectFailure();
    }

    @Override
    public void onDeviceDisconnect(boolean isActive) {
        ViseLog.e(this + " : callback wrong");
    }

    @Override
    public String getStateDescription() {
        return "连接断开";
    }

    @Override
    public boolean canConnect() {
        return true;
    }

    @Override
    public boolean canDisconnect() {
        return false;
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
