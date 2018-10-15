package com.cmtech.android.bledevicecore.model;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;

public class BleDataOpCallbackAdapter implements IBleCallback {
    private IBleDataOpCallback dataOpCallback;

    public BleDataOpCallbackAdapter(IBleDataOpCallback dataOpCallback) {
        if(dataOpCallback == null) {
            throw new IllegalArgumentException();
        }

        this.dataOpCallback = dataOpCallback;
    }

    @Override
    public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
        dataOpCallback.onSuccess(data);
    }

    @Override
    public void onFailure(BleException exception) {
        dataOpCallback.onFailure(new BleDataOpException(exception));
    }
}
