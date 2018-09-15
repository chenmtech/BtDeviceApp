package com.cmtech.android.btdevice.temphumid;

import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceController;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;

public class TempHumidController extends BleDeviceController {
    public TempHumidController(BleDevice device) {
        super(device);
    }

    public void updateHistoryData() {
        ((TempHumidDevice)getDevice()).updateHistoryData();
    }
}
