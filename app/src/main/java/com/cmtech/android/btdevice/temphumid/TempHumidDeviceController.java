package com.cmtech.android.btdevice.temphumid;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceModelInterface;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;

public class TempHumidDeviceController extends BLEDeviceController {
    public TempHumidDeviceController(IBLEDeviceModelInterface device, MainActivity activity) {
        super(device, activity);
    }

    public void initDeviceTimerService() {

    }
}
