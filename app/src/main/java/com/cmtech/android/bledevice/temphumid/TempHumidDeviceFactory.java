package com.cmtech.android.bledevice.temphumid;

import com.cmtech.android.bledeviceapp.model.BleDeviceAbstractFactory;
import com.cmtech.android.bledeviceapp.model.BleDeviceController;
import com.cmtech.android.bledeviceapp.model.BleDeviceFragment;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;

public class TempHumidDeviceFactory extends BleDeviceAbstractFactory {
    @Override
    public BleDevice createBleDevice(BleDeviceBasicInfo basicInfo) {
        return new TempHumidDevice(basicInfo);
    }

    @Override
    public BleDeviceController createController(BleDevice device) {
        return new TempHumidController(device);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return new TempHumidFragment();
    }


}
