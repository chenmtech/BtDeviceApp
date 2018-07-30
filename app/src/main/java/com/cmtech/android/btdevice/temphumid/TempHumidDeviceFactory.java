package com.cmtech.android.btdevice.temphumid;

import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceController;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;

public class TempHumidDeviceFactory extends BleDeviceAbstractFactory {
    @Override
    public BleDevice createBleDevice(BleDeviceBasicInfo basicInfo) {
        return new TempHumidDevice(basicInfo);
    }

    @Override
    public BleDeviceController createController(BleDevice device) {
        return new TempHumidDeviceController(device);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return new TempHumidFragment();
    }


}
