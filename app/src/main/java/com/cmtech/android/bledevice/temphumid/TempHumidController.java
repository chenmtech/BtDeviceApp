package com.cmtech.android.bledevice.temphumid;

import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceController;

public class TempHumidController extends BleDeviceController {
    public TempHumidController(BleDevice device) {

        super(device);
    }

    public void updateHistoryData() {
        ((TempHumidDevice)getDevice()).updateHistoryData();
    }
}
