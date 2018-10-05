package com.cmtech.android.bledevice.temphumid;

import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledeviceapp.model.BleDeviceController;

public class TempHumidController extends BleDeviceController {
    public TempHumidController(BleDevice device) {

        super(device);

        configureActivityClass = TempHumidConfigureActivity.class;
    }

    public void updateHistoryData() {
        ((TempHumidDevice)getDevice()).updateHistoryData();
    }
}
