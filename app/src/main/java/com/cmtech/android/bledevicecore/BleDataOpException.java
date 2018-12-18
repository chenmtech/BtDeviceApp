package com.cmtech.android.bledevicecore;

import com.cmtech.android.ble.exception.BleException;

public class BleDataOpException extends BleException {
    public BleDataOpException(BleException exception) {
        super(exception.getCode(), exception.getDescription());
    }

    @Override
    public String toString() {
        return "BleDataOpException{" +
                "code=" + getCode() +
                ", description='" + getDescription() + '\'' +
                '}';
    }
}
