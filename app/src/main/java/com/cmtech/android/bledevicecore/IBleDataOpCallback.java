package com.cmtech.android.bledevicecore;


public interface IBleDataOpCallback {
    void onSuccess(byte[] data);

    void onFailure(BleDataOpException exception);
}
