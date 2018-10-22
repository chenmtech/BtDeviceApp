package com.cmtech.android.bledevicecore.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceUtil;
import com.vise.log.ViseLog;

public class BleDeviceCloseState implements IBleDeviceState {
    private BleDevice device;

    public BleDeviceCloseState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void open() {
        device.setState(device.getDisconnectState());
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
        ViseLog.e(this + " : have closed!");
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        ViseLog.e(this + " : have closed!");
        //MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(mirror);
        BleDeviceUtil.removeDeviceMirror(mirror);
    }

    @Override
    public void onDeviceConnectFailure() {
        ViseLog.e(this + " : have closed!");
    }

    @Override
    public void onDeviceDisconnect(boolean isActive) {
        ViseLog.e(this + " : have closed!");
        device.onStateDisconnect(isActive);
    }

    @Override
    public String getStateDescription() {
        return "关闭";
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
