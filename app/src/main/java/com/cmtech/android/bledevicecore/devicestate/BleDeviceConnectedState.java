package com.cmtech.android.bledevicecore.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceUtil;
import com.vise.log.ViseLog;

public class BleDeviceConnectedState implements IBleDeviceState {
    private BleDevice device;

    public BleDeviceConnectedState(BleDevice device) {
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
        device.disconnect();
    }

    @Override
    public void onDeviceScanFinish(boolean result) {
        ViseLog.e(this + " : callback wrong");
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        ViseLog.e(this + " : callback wrong");
        // 重复多次成功连接，需要把后一次移除。
        //MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(mirror);
        //BleDeviceUtil.removeDeviceMirror(mirror);
        //device.onStateConnectSuccess(mirror);
    }

    @Override
    public void onDeviceConnectFailure() {
        device.onStateConnectFailure();
    }

    @Override
    public void onDeviceDisconnect(boolean isActive) {
        device.onStateDisconnect(isActive);
    }

    @Override
    public String getStateDescription() {
        return "连接成功";
    }

    @Override
    public boolean canConnect() {
        return false;
    }

    @Override
    public boolean canDisconnect() {
        return true;
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
