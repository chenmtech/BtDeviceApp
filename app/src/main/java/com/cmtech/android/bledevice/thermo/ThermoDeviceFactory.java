package com.cmtech.android.bledevice.thermo;

import com.cmtech.android.bledevicecore.model.BleDeviceAbstractFactory;
import com.cmtech.android.bledevicecore.model.BleDeviceController;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;

public class ThermoDeviceFactory extends BleDeviceAbstractFactory {
    @Override
    public BleDevice createBleDevice(BleDeviceBasicInfo basicInfo) {
        return new ThermoDevice(basicInfo);
    }

    @Override
    public BleDeviceController createController(BleDevice device) {
        return new ThermoController(device);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return new ThermoFragment();
    }
}
