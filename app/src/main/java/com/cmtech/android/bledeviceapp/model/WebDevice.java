package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.bledevice.ecgmonitorweb.EcgBroadcastReceiver;
import com.vise.log.ViseLog;

import java.util.List;

import static com.cmtech.android.ble.core.BleDeviceState.CLOSED;
import static com.cmtech.android.ble.core.BleDeviceState.CONNECTING;
import static com.cmtech.android.ble.core.BleDeviceState.DISCONNECT;
import static com.cmtech.android.ble.core.BleDeviceState.CONNECT;

public abstract class WebDevice extends AbstractDevice {
    public WebDevice(BleDeviceRegisterInfo registerInfo) {
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
        setState(CONNECTING);

        EcgBroadcastReceiver.retrieveBroadcastIdList("", new EcgBroadcastReceiver.IEcgBroadcastIdListCallback() {
            @Override
            public void onReceived(List<String> broadcastIdList) {
                if(broadcastIdList == null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            setState(DISCONNECT);
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            setState(CONNECT);
                            executeAfterConnectSuccess();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void switchState() {
        ViseLog.e("BleDevice.switchState()");
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

    }

    @Override
    public void clear() {

    }

    @Override
    protected abstract boolean executeAfterConnectSuccess();

    @Override
    protected abstract void executeAfterConnectFailure();

    @Override
    protected abstract void executeAfterDisconnect();
}
