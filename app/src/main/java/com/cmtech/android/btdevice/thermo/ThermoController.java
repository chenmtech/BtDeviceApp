package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BleDeviceController;

public class ThermoController extends BleDeviceController {
    public ThermoController(IBleDeviceInterface device, MainActivity activity) {
        super(device, activity);
    }
}
