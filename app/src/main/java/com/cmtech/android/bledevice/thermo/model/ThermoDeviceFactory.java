package com.cmtech.android.bledevice.thermo.model;

import com.cmtech.android.bledevice.thermo.activity.ThermoFragment;
import com.cmtech.android.bledevicecore.AbstractBleDeviceFactory;
import com.cmtech.android.bledevicecore.BleDevice;
import com.cmtech.android.bledevicecore.BleDeviceFragment;

public class ThermoDeviceFactory extends AbstractBleDeviceFactory {
    @Override
    public BleDevice createBleDevice() {
        return new ThermoDevice(basicInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return ThermoFragment.newInstance(basicInfo.getMacAddress());
    }
}
