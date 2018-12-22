package com.cmtech.android.bledevice.temphumid.model;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorGattOperator;
import com.cmtech.android.bledevice.temphumid.activity.TempHumidFragment;
import com.cmtech.android.bledevice.core.AbstractBleDeviceFactory;
import com.cmtech.android.bledevice.core.BleDevice;
import com.cmtech.android.bledevice.core.BleDeviceFragment;

// 会根据设备类型BleDeviceType，通过反射创建工厂类实例
public class TempHumidDeviceFactory extends AbstractBleDeviceFactory {
    @Override
    public BleDevice createBleDevice() {
        TempHumidGattOperator gattOperator = new TempHumidGattOperator();
        return new TempHumidDevice(basicInfo, gattOperator);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return TempHumidFragment.newInstance(basicInfo.getMacAddress());
    }


}
