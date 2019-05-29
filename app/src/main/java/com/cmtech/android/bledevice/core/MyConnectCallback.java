package com.cmtech.android.bledevice.core;

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;
import com.vise.log.ViseLog;

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
    public void onConnectSuccess(DeviceMirror deviceMirror) {
        ViseLog.e("Connect Success Thread: " + Thread.currentThread());

        device.processConnectSuccess(deviceMirror);
    }

    @Override
    public void onConnectFailure(BleException exception) {
        ViseLog.e("Connect Failure Thread: " + Thread.currentThread());

        device.processConnectFailure(exception);
    }

    @Override
    public void onDisconnect(boolean isActive) {
        ViseLog.e("onDisconnect Thread: " + Thread.currentThread());

        device.processDisconnect(isActive);
    }





}
