package com.cmtech.android.bledeviceapp.model;

import android.content.Context;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.ble.core.IDeviceConnector;
import com.vise.log.ViseLog;

import static com.cmtech.android.ble.core.BleDeviceState.CLOSED;
import static com.cmtech.android.ble.core.BleDeviceState.CONNECT;
import static com.cmtech.android.ble.core.BleDeviceState.DISCONNECT;
import static com.cmtech.android.ble.core.BleDeviceState.FAILURE;
import static com.cmtech.android.ble.core.IDevice.MSG_INVALID_OPERATION;

public class WebDeviceConnector implements IDeviceConnector {
    private final AbstractDevice device;
    private volatile BleDeviceState state = CLOSED; // 实时状态

    public WebDeviceConnector(AbstractDevice device) {
        this.device = device;
    }

    @Override
    public void open(Context context) {
        if(device.getState() != CLOSED) {
            ViseLog.e("The device is opened.");
            return;
        }
        if(context == null) {
            throw new NullPointerException("The context is null.");
        }

        ViseLog.e("WebDeviceConnector.open()");

        device.setState(DISCONNECT);
        if(device.autoConnect()) {
            connect();
        }
    }

    private void connect() {
        device.setState(CONNECT);
        if(!device.onConnectSuccess())
            forceDisconnect(true);
    }

    @Override
    public void switchState() {
        ViseLog.e("WebDeviceConnector.switchState()");
        if(isDisconnected()) {
            connect();
        } else if(isConnected()) {
            forceDisconnect(true);
        } else { // 无效操作
            device.notifyExceptionMessage(MSG_INVALID_OPERATION);
        }
    }

    @Override
    public void forceDisconnect(boolean forever) {
        device.setState(DISCONNECT);
    }

    @Override
    public boolean isDisconnectedForever() {
        return isDisconnected();
    }

    @Override
    public void close() {
        if(!isDisconnectedForever()) {
            ViseLog.e("The device can't be closed currently.");
            return;
        }

        ViseLog.e("WebDeviceConnector.close()");

        device.setState(BleDeviceState.CLOSED);
    }

    @Override
    public void clear() {

    }

    @Override
    public BleDeviceState getState() {
        return state;
    }

    @Override
    public void setState(BleDeviceState state) {
        if(this.state != state) {
            ViseLog.e("The state of device " + device.getAddress() + " is " + state);
            this.state = state;
            device.updateState();
        }
    }

    @Override
    public boolean isConnected() {
        return device.getState() == CONNECT;
    }
    @Override
    public boolean isDisconnected() {
        return device.getState() == FAILURE || device.getState() == DISCONNECT;
    }

    @Override
    public boolean isLocal() {
        return false;
    }
}
