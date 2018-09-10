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
    public void open() {
        ViseLog.i("action wrong");
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
        device.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                device.processDisconnect();
                device.setState(device.getOpenState());
            }
        }, 5000);

        MyApplication.getViseBle().getDeviceMirrorPool().disconnect(device.getBluetoothLeDevice());
        device.setState(device.getDisconnectingState());
    }

    @Override
    public void switchState() {
        disconnect();
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
        device.getHandler().removeCallbacksAndMessages(null);
        device.processDisconnect();
        device.setState(device.getOpenState());
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
        return false;
    }
}
