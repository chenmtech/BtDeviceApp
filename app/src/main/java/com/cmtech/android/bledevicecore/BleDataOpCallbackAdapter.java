package com.cmtech.android.bledevicecore;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;

/**
 * BleDataOpCallbackAdapter: Ble数据操作回调适配器，将IBleDataOpCallback适配为ViseBle包中的IBleCallback
 * Created by bme on 2018/3/1.
 */

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
