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
        ViseLog.i("have closed!");
    }

    @Override
    public void onDeviceScanFailure() {
        ViseLog.i("have closed!");
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        ViseLog.i("have closed!");
        if(mirror != null)
            MyApplication.getViseBle().getDeviceMirrorPool().disconnect(mirror.getBluetoothLeDevice());
    }

    @Override
    public void onDeviceConnectFailure() {
        ViseLog.i("have closed!");
    }

    @Override
    public void onDeviceConnectTimeout() {
        ViseLog.i("have closed!");
    }

    @Override
    public void onDeviceDisconnect() {
        ViseLog.i("have closed!");
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
