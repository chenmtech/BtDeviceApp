package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.activity.EcgMonitorFragment;
import com.cmtech.android.bledevice.core.AbstractBleDeviceFactory;
import com.cmtech.android.bledevice.core.BleDevice;
import com.cmtech.android.bledevice.core.BleDeviceFragment;

// 根据设备类型BleDeviceType，通过反射创建工厂类实例
public class EcgMonitorDeviceFactory extends AbstractBleDeviceFactory {
    @Override
    public BleDevice createDevice() {
        return new EcgMonitorDevice(basicInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return BleDeviceFragment.create(basicInfo.getMacAddress(), EcgMonitorFragment.class);
    }
}
