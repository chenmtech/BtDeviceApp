package com.cmtech.android.btdevice.temphumid;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.BLEDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.model.BLEDeviceBasicInfo;

public class TempHumidDeviceFactory extends BLEDeviceAbstractFactory {
    @Override
    public IBLEDeviceInterface createDevice(BLEDeviceBasicInfo basicInfo) {
        return new TempHumidDevice(basicInfo);
    }

    @Override
    public IBLEDeviceControllerInterface createController(IBLEDeviceInterface device, MainActivity activity) {
        return new TempHumidDeviceController(device, activity);
    }

    @Override
    public BLEDeviceFragment createFragment() {
        return new TempHumidFragment();
    }
}
