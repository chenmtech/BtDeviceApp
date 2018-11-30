package com.cmtech.android.bledevicecore.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceUtil;
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
            BleDeviceUtil.connect(device.getBluetoothLeDevice(), device.getConnectCallback());
            device.setState(device.getConnectingState());
        } else {
            device.onStateScanFailure();
        }
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        device.onStateConnectSuccess(mirror);
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
