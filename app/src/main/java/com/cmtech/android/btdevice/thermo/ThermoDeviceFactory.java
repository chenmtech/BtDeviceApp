package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;

public class ThermoDeviceFactory extends BleDeviceAbstractFactory {
    @Override
    public IBleDeviceInterface createBleDevice(BleDeviceBasicInfo basicInfo) {
        return new ThermoDevice(basicInfo);
    }

    @Override
    public IBleDeviceControllerInterface createController(IBleDeviceInterface device, MainActivity activity) {
        return new ThermoController(device, activity);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return new ThermoFragment();
    }
}
