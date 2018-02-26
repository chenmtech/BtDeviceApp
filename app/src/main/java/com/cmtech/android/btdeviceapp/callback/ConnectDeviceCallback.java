package com.cmtech.android.btdeviceapp.callback;

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;

/**
 * Created by bme on 2018/2/26.
 */

public class ConnectDeviceCallback implements IConnectCallback {
    private final int deviceItem;

    ConnectDeviceCallback(int deviceItem) {
        this.deviceItem = deviceItem;
    }

    @Override
    public void onConnectSuccess(DeviceMirror deviceMirror) {

    }

    @Override
    public void onConnectFailure(BleException exception) {

    }

    @Override
    public void onDisconnect(boolean isActive) {

    }
}
