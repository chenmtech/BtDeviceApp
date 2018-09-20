package com.cmtech.android.bledevice.ecgmonitor;

import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledeviceapp.model.BleDeviceController;
import com.cmtech.android.bledeviceapp.model.BleDeviceFragment;
import com.cmtech.android.bledevicecore.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;

public class EcgMonitorDeviceFactory extends BleDeviceAbstractFactory {
    @Override
    public BleDevice createBleDevice(BleDeviceBasicInfo basicInfo) {
        return new EcgMonitorDevice(basicInfo);
    }

    @Override
    public BleDeviceController createController(BleDevice device) {
        return new EcgMonitorDeviceController(device);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return new EcgMonitorFragment();
    }
}
