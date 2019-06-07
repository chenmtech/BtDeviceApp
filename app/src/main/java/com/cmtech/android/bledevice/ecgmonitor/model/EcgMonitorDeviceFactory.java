package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;

import com.cmtech.android.ble.extend.AbstractBleDeviceFactory;
import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceFragment;
import com.cmtech.android.ble.extend.BleDeviceType;
import com.cmtech.android.bledevice.ecgmonitor.activity.EcgMonitorFragment;
import com.cmtech.android.bledeviceapp.R;

// 根据设备类型BleDeviceType，通过反射创建工厂类实例
public class EcgMonitorDeviceFactory extends AbstractBleDeviceFactory {
    private static final String UUID_ECGMONITOR                 = "aa40";       // 心电监护仪

    private static final String NAME_ECGMONITOR                  = "心电带";

    private static final int IMAGE_ECGMONITOR                  = R.drawable.ic_ecgmonitor_defaultimage;

    private static final String ecgMonitorFactory = "com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDeviceFactory";

    public static final BleDeviceType ECGMONITOR_DEVICE_TYPE = new BleDeviceType(UUID_ECGMONITOR, IMAGE_ECGMONITOR, NAME_ECGMONITOR, ecgMonitorFactory);

    @Override
    public BleDevice createDevice(Context context) {
        return new EcgMonitorDevice(context, basicInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return BleDeviceFragment.create(basicInfo.getMacAddress(), EcgMonitorFragment.class);
    }
}
