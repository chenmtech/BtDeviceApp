package com.cmtech.android.btdevice.ecgmonitor;

import com.cmtech.android.btdeviceapp.interfa.IBleDeviceController;
import com.cmtech.android.btdeviceapp.interfa.IBleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;

public class EcgMonitorDeviceFactory extends BleDeviceAbstractFactory {
    @Override
    public IBleDevice createBleDevice(BleDeviceBasicInfo basicInfo) {
        return new EcgMonitorDevice(basicInfo);
    }

    @Override
    public IBleDeviceController createController(IBleDevice device) {
        return new EcgMonitorDeviceController(device);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return new EcgMonitorFragment();
    }
}
