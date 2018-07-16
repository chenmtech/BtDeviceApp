package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceController;
import com.cmtech.android.btdeviceapp.interfa.IBleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;

public class ThermoDeviceFactory extends BleDeviceAbstractFactory {
    @Override
    public IBleDevice createBleDevice(BleDeviceBasicInfo basicInfo) {
        return new ThermoDevice(basicInfo);
    }

    @Override
    public IBleDeviceController createController(IBleDevice device) {
        return new ThermoController(device);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return new ThermoFragment();
    }
}
