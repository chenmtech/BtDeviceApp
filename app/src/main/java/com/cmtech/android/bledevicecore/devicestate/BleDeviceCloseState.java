package com.cmtech.android.bledevicecore.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceCloseState implements IBleDeviceState {
    private BleDevice device;

    public BleDeviceCloseState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void open() {
        device.setState(device.getOpenState());
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
        ViseLog.i("callback wrong");
        if(mirror != null)
            MyApplication.getViseBle().getDeviceMirrorPool().disconnect(mirror.getBluetoothLeDevice());
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
