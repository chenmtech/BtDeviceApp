package com.cmtech.android.bledevicecore.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceDisconnectingState implements IBleDeviceState {
    private BleDevice device;

    public BleDeviceDisconnectingState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void close() {
        ViseLog.e(this + " : action wrong");
    }

    @Override
    public void switchState() {
        ViseLog.e(this + " : action wrong");
    }

    @Override
    public void onDeviceScanFinish(boolean result) {
        ViseLog.e(this + " : callback wrong");
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        device.disconnect();
    }

    @Override
    public void onDeviceConnectFailure() {
        ViseLog.e(this + " : callback wrong");
    }

    @Override
    public void onDeviceDisconnect(boolean isActive) {
        device.onStateDisconnect(isActive);
    }

    @Override
    public String getStateDescription() {
        return "断开中";
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
        return true;
    }
}
