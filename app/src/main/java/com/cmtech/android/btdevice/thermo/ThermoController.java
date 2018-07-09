package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;

public class ThermoController extends BLEDeviceController {
    public ThermoController(IBLEDeviceInterface device, MainActivity activity) {
        super(device, activity);
    }
}
