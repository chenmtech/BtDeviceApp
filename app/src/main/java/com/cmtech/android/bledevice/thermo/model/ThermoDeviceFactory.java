package com.cmtech.android.bledevice.thermo.model;

import com.cmtech.android.bledevice.thermo.activity.ThermoFragment;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.AbstractBleDeviceFactory;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;

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
