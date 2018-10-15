package com.cmtech.android.bledevicecore.model;


public interface IBleDataOpCallback {
    void onSuccess(byte[] data);

    void onFailure(BleDataOpException exception);
}
