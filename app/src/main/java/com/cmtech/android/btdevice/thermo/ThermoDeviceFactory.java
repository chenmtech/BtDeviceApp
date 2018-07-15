package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.MainController;

public class ThermoDeviceFactory extends BleDeviceAbstractFactory {
    @Override
    public IBleDeviceInterface createBleDevice(BleDeviceBasicInfo basicInfo) {
        return new ThermoDevice(basicInfo);
    }

    @Override
    public IBleDeviceControllerInterface createController(IBleDeviceInterface device, MainController mainController) {
        return new ThermoController(device, mainController);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return new ThermoFragment();
    }
}
