package com.cmtech.android.btdeviceapp.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceCloseState implements IBleDeviceState {
    BleDevice device;

    public BleDeviceCloseState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void deviceOpen() {
        device.getHandler().removeCallbacksAndMessages(null);

        device.setState(device.getOpenState());
    }

    @Override
    public void deviceClose() {
        ViseLog.i("action wrong");
    }

    @Override
    public void deviceStartScan() {
        ViseLog.i("action wrong");
    }

    @Override
    public void deviceDisconnect() {
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
    public String deviceGetStateInfo() {
        return "关闭";
    }
}
