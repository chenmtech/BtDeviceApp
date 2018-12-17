package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.activity.EcgMonitorFragment;
import com.cmtech.android.bledeviceapp.model.UserAccount;
import com.cmtech.android.bledevicecore.BleDevice;
import com.cmtech.android.bledevicecore.AbstractBleDeviceFactory;
import com.cmtech.android.bledevicecore.BleDeviceFragment;

import org.litepal.LitePal;

import java.util.List;

// 会根据设备类型BleDeviceType，通过反射创建工厂类实例
public class EcgMonitorDeviceFactory extends AbstractBleDeviceFactory {
    @Override
    public BleDevice createBleDevice() {
        return new EcgMonitorDevice(basicInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return EcgMonitorFragment.newInstance(basicInfo.getMacAddress());
    }
}
