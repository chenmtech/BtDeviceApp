package com.cmtech.android.btdevice.temphumid;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.BLEDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;
import com.cmtech.android.btdeviceapp.model.BLEDevicePersistantInfo;

public class TempHumidDeviceFactory extends BLEDeviceAbstractFactory {
    @Override
    public BLEDeviceModel createDevice(BLEDevicePersistantInfo persistantInfo) {
        return new TempHumidDevice(persistantInfo);
    }

    @Override
    public BLEDeviceController createController(BLEDeviceModel device, MainActivity activity) {
        return new TempHumidDeviceController(device, activity);
    }

    @Override
    public BLEDeviceFragment createFragment() {
        return new TempHumidFragment();
    }
}
