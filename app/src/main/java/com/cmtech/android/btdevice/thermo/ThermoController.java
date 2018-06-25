package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;

public class ThermoController extends BLEDeviceController {
    public ThermoController(BLEDeviceModel device, MainActivity activity) {
        super(device, activity);
    }
}
