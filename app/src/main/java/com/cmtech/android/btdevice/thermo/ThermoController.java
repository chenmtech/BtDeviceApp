package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BleDeviceController;
import com.cmtech.android.btdeviceapp.model.MainController;

public class ThermoController extends BleDeviceController {
    public ThermoController(IBleDeviceInterface device, MainController mainController) {
        super(device, mainController);
    }
}
