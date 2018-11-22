package com.cmtech.android.bledevice.thermo;

import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceAbstractFactory;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;

public class ThermoDeviceFactory extends BleDeviceAbstractFactory {
    @Override
    public BleDevice createBleDevice(BleDeviceBasicInfo basicInfo) {
        return new ThermoDevice(basicInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return new ThermoFragment();
    }
}
