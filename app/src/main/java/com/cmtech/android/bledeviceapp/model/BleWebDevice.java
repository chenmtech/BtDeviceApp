package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;

public abstract class BleWebDevice extends AbstractDevice {
    public BleWebDevice(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }


}
