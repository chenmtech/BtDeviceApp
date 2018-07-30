package com.cmtech.android.btdevice.ecgmonitor;

import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceController;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;

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
