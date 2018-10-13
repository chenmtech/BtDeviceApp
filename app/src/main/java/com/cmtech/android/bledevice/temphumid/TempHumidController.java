package com.cmtech.android.bledevice.temphumid;

import com.cmtech.android.bledeviceapp.model.BleDeviceController;
import com.cmtech.android.bledevicecore.model.BleDevice;

public class TempHumidController extends BleDeviceController {
    public TempHumidController(BleDevice device) {

        super(device);
    }

    public void updateHistoryData() {
        ((TempHumidDevice)getDevice()).updateHistoryData();
    }
}
