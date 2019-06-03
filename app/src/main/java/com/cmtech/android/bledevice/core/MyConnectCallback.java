package com.cmtech.android.bledevice.core;

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;

/**
 * MyConnectCallback: 我的连接回调类，负责连接回调的处理
 * Created by bme on 2018/12/23.
 */

public class MyConnectCallback implements IConnectCallback {
    private final BleDevice device; // 设备


    MyConnectCallback(BleDevice device) {
        this.device = device;
    }

    @Override
    public void onConnectSuccess(final DeviceMirror deviceMirror) {
        device.postDelayed(new Runnable() {
            @Override
            public void run() {
                device.processConnectSuccess(deviceMirror);
            }
        }, 0);

    }

    @Override
    public void onConnectFailure(final BleException exception) {
        device.postDelayed(new Runnable() {
            @Override
            public void run() {
                device.processConnectFailure(exception);
            }
        }, 0);

    }

    @Override
    public void onDisconnect(final boolean isActive) {
        device.postDelayed(new Runnable() {
            @Override
            public void run() {
                device.processDisconnect(isActive);
            }
        }, 0);

    }





}
