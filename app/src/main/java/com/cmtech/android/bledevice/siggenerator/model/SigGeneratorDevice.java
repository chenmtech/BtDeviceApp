package com.cmtech.android.bledevice.siggenerator.model;

import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceBasicInfo;

public class SigGeneratorDevice extends BleDevice {

    // 构造器
    public SigGeneratorDevice(BleDeviceBasicInfo basicInfo) {
        super(basicInfo);

    }

    @Override
    protected void executeAfterConnectSuccess() {

    }

    @Override
    protected void executeAfterConnectFailure() {

    }

    @Override
    protected void executeAfterDisconnect() {

    }
}
