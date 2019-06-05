package com.cmtech.android.bledevice.temphumid.model;

import com.cmtech.android.bledevice.ecgmonitor.activity.EcgMonitorFragment;
import com.cmtech.android.bledevice.temphumid.activity.TempHumidFragment;
import com.cmtech.android.bledevice.core.AbstractBleDeviceFactory;
import com.cmtech.android.bledevice.core.BleDevice;
import com.cmtech.android.bledevice.core.BleDeviceFragment;

// 会根据设备类型BleDeviceType，通过反射创建工厂类实例
public class TempHumidDeviceFactory extends AbstractBleDeviceFactory {
    @Override
    public BleDevice createDevice() {
        return new TempHumidDevice(basicInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return BleDeviceFragment.create(basicInfo.getMacAddress(), TempHumidFragment.class);
    }


}
