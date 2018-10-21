package com.cmtech.android.bledevicecore.devicestate;

import android.os.Handler;
import android.os.Looper;

import com.cmtech.android.ble.callback.scan.IScanCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceScanState implements IBleDeviceState {
    private BleDevice device;

    public BleDeviceScanState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void open() {
        ViseLog.e(this + " : action wrong");
    }

    @Override
    public void close() {
        device.disconnect();
    }

    @Override
    public void switchState() {
        ViseLog.e(this + " : action wrong");
    }

    @Override
    public void onDeviceScanFinish(boolean result) {
        if(result) {
            device.setState(device.getConnectingState());
        } else {
            device.processScanFailure();
        }
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        device.processConnectSuccess(mirror);
    }

    @Override
    public void onDeviceConnectFailure() {
        ViseLog.e(this + " : callback wrong");
    }

    @Override
    public void onDeviceDisconnect(boolean isActive) {
        ViseLog.e(this + " : callback wrong");
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
        return true;
    }
}
