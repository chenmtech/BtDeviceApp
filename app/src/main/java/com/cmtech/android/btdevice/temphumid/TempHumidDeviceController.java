package com.cmtech.android.btdevice.temphumid;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;

public class TempHumidDeviceController extends BLEDeviceController {
    public TempHumidDeviceController(IBLEDeviceInterface device, MainActivity activity) {
        super(device, activity);
    }

    public void initDeviceTimerService() {

    }
}
