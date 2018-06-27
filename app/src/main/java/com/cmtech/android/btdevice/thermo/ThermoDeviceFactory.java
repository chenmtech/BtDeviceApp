package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.BLEDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceModelInterface;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;
import com.cmtech.android.btdeviceapp.model.BLEDeviceBasicInfo;

public class ThermoDeviceFactory extends BLEDeviceAbstractFactory {
    @Override
    public IBLEDeviceModelInterface createDevice(BLEDeviceBasicInfo basicInfo) {
        return new ThermoDevice(basicInfo);
    }

    @Override
    public IBLEDeviceControllerInterface createController(IBLEDeviceModelInterface device, MainActivity activity) {
        return new ThermoController(device, activity);
    }

    @Override
    public BLEDeviceFragment createFragment() {
        return new ThermoFragment();
    }
}
