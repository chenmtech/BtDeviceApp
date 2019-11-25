package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.bledevice.ecgmonitorweb.EcgHttpReceiver;
import com.vise.log.ViseLog;

import static com.cmtech.android.ble.core.BleDeviceState.CLOSED;
import static com.cmtech.android.ble.core.BleDeviceState.CONNECT;
import static com.cmtech.android.ble.core.BleDeviceState.CONNECTING;
import static com.cmtech.android.ble.core.BleDeviceState.DISCONNECT;

public class WebDevice extends AbstractDevice {
    public WebDevice(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public void open(Context context) {
        if(state != CLOSED) {
            ViseLog.e("The device is opened.");
            return;
        }
        if(context == null) {
            throw new NullPointerException("The context is null.");
        }

        ViseLog.e("WebDevice.open()");

        setState(DISCONNECT);
        if(registerInfo.autoConnect()) {
            connect();
        }
    }

    private void connect() {
        setState(CONNECT);
        if(callback != null) {
            if(!callback.onConnectSuccess())
                callDisconnect(true);
        }
    }

    @Override
    public void switchState() {
        ViseLog.e("WebDevice.switchState()");
        if(isDisconnected()) {
            connect();
        } else if(isConnected()) {
            callDisconnect(true);
        } else { // 无效操作
            notifyExceptionMessage(MSG_INVALID_OPERATION);
        }
    }

    @Override
    public void callDisconnect(boolean stopAutoScan) {
        setState(DISCONNECT);
    }

    @Override
    public boolean isStopped() {
        return isDisconnected();
    }

    @Override
    public void close() {
        if(!isStopped()) {
            ViseLog.e("The device can't be closed currently.");
            return;
        }

        ViseLog.e("WebDevice.close()");

        setState(BleDeviceState.CLOSED);
    }

    @Override
    public void clear() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean isLocal() {
        return false;
    }
}
