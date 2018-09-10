package com.cmtech.android.btdeviceapp.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceConnectedState implements IBleDeviceState {
    BleDevice device;

    public BleDeviceConnectedState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void deviceOpen() {
        ViseLog.i("action wrong");
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
        device.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                device.setBluetoothLeDevice(null);
                device.setState(device.getOpenState());
            }
        }, 5000);

        MyApplication.getViseBle().getDeviceMirrorPool().disconnect(device.getBluetoothLeDevice());
        device.setState(device.getDisconnectingState());
    }

    @Override
    public void onDeviceScanSuccess() {

    }

    @Override
    public void onDeviceScanFailure() {

    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {

    }

    @Override
    public void onDeviceConnectFailure() {

    }

    @Override
    public void onDeviceConnectTimeout() {

    }

    @Override
    public void onDeviceDisconnect() {

    }

    @Override
    public String deviceGetStateInfo() {
        return "连接成功";
    }
}
