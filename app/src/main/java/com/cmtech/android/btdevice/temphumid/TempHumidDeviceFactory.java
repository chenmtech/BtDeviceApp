package com.cmtech.android.btdevice.temphumid;

import com.cmtech.android.btdeviceapp.interfa.IBleDeviceController;
import com.cmtech.android.btdeviceapp.interfa.IBleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;

public class TempHumidDeviceFactory extends BleDeviceAbstractFactory {
    @Override
    public IBleDevice createBleDevice(BleDeviceBasicInfo basicInfo) {
        return new TempHumidDevice(basicInfo);
    }

    @Override
    public IBleDeviceController createController(IBleDevice device) {
        return new TempHumidDeviceController(device);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return new TempHumidFragment();
    }


}
