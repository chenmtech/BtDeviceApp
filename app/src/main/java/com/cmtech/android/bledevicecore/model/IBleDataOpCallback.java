package com.cmtech.android.bledevicecore.model;

import com.cmtech.android.ble.exception.BleException;

public interface IBleDataOpCallback {
    void onSuccess(byte[] data);

    void onFailure(BleDataOpException exception);
}
