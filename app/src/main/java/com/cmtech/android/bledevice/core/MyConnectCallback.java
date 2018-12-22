package com.cmtech.android.bledevice.core;

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;
import com.vise.log.ViseLog;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.RECONNECT_INTERVAL;

/**
 * MyConnectCallback: 我的连接回调类，负责连接回调的处理
 * Created by bme on 2018/12/23.
 */

public class MyConnectCallback implements IConnectCallback {

    private final BleDevice device;
    private boolean connectSuccess = false; // 标记是否刚刚已经执行了onConnectSuccess，为了防止有时候出现连续两次执行的异常情况。这个异常是BLE包的问题
    private int curReconnectTimes = 0; // 当前的重连次数

    public void clearReconnectTimes() {
        curReconnectTimes = 0;
    }

    public MyConnectCallback(BleDevice device) {
        this.device = device;
    }

    @Override
    public void onConnectSuccess(DeviceMirror deviceMirror) {
        processConnectSuccess(deviceMirror);
    }

    @Override
    public void onConnectFailure(BleException exception) {
        processConnectFailure(exception);
    }

    @Override
    public void onDisconnect(boolean isActive) {
        processDisconnect(isActive);
    }

    // 连接成功回调处理
    private void processConnectSuccess(DeviceMirror mirror) {
        ViseLog.i("onConnectSuccess");
        // 防止多次连接成功
        if(connectSuccess) {
            return;
        }

        if(device.isClosing()) {
            BleDeviceUtil.disconnect(device);
            return;
        }

        device.removeCallbacksAndMessages();
        mirror.registerStateObserver(device);
        device.setConnectState(BleDeviceConnectState.getFromCode(mirror.getConnectState().getCode()));

        // 命令设备连接后的处理
        if(!device.executeAfterConnectSuccess()) {
            device.disconnect();
        }
        curReconnectTimes = 0;
        connectSuccess = true;
    }

    // 连接错误回调处理
    private void processConnectFailure(final BleException bleException) {
        ViseLog.e("onConnectFailure: " + bleException);

        if(device.isClosing())
            return;

        // 仍然有可能会连续执行两次下面语句
        device.executeAfterConnectFailure();
        device.removeCallbacksAndMessages();
        reconnect();
        connectSuccess = false;
    }

    // 连接断开回调处理
    private void processDisconnect(boolean isActive) {
        ViseLog.e("onDisconnect " + isActive);

        if(device.isClosing()) {
            device.setConnectState(BleDeviceConnectState.CONNECT_CLOSED);
        } else if(!isActive) {
            device.executeAfterConnectFailure();
            device.removeCallbacksAndMessages();
        }
        connectSuccess = false;
    }

    // 重新连接
    private void reconnect() {
        int canReconnectTimes = device.getReconnectTimes();
        if(curReconnectTimes < canReconnectTimes || canReconnectTimes == -1) {
            device.postDelayed(new Runnable() {
                @Override
                public void run() {
                    device.startConnect();
                }
            }, RECONNECT_INTERVAL);
            if(curReconnectTimes < canReconnectTimes)
                curReconnectTimes++;
        } else if(device.getBasicInfo().isWarnAfterReconnectFailure()) {
            device.notifyReconnectFailureObservers(true);
        }
    }
}
