package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceController;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;

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
