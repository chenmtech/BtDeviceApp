package com.cmtech.android.bledevicecore.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceDisconnectState implements IBleDeviceState {
    private BleDevice device;

    public BleDeviceDisconnectState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void open() {
        ViseLog.i("action wrong");
    }

    @Override
    public void close() {
        device.setState(device.getCloseState());
    }

    @Override
    public void scan() {
        device.getHandler().removeCallbacksAndMessages(null);
        DeviceMirror deviceMirror = MyApplication.getViseBle().getDeviceMirror(device.getBluetoothLeDevice());
        if(deviceMirror != null)
            deviceMirror.clear();
        MyApplication.getViseBle().connectByMac(device.getMacAddress(), device.getConnectCallback());
        device.setState(device.getScanState());
    }

    @Override
    public void disconnect() {
        ViseLog.i("action wrong");
    }

    @Override
    public void switchState() {
        scan();
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
        device.setState(device.getConnectedState());
        device.processConnectSuccess(mirror);
    }

    @Override
    public void onDeviceConnectFailure() {
        ViseLog.i("callback wrong");
        device.processConnectFailure();
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
        return "连接断开";
    }

    @Override
    public boolean canConnect() {
        return true;
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
