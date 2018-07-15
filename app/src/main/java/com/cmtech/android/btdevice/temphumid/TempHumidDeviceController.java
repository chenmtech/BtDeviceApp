package com.cmtech.android.btdevice.temphumid;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BleDeviceController;
import com.cmtech.android.btdeviceapp.model.MainController;

public class TempHumidDeviceController extends BleDeviceController {
    public TempHumidDeviceController(IBleDeviceInterface device, MainController mainController) {
        super(device, mainController);
    }


}
