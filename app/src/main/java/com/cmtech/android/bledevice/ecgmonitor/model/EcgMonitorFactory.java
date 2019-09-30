package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;

import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.bledeviceapp.activity.BleFragment;
import com.cmtech.android.bledeviceapp.model.BleDeviceFactory;
import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceType;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgMonitorFragment;
import com.cmtech.android.bledeviceapp.R;

// 根据设备类型BleDeviceType，通过反射创建工厂类实例
public class EcgMonitorFactory extends BleDeviceFactory {
    private static final String ECGMONITOR_UUID = "aa40"; // 心电监护仪支持的服务UUID短串
    private static final String ECGMONITOR_NAME = "心电带";
    private static final int ECGMONITOR_IMAGE = R.drawable.ic_ecgmonitor_defaultimage;
    private static final String ECGMONITOR_FACTORY = "com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorFactory";

    public static final BleDeviceType ECGMONITOR_DEVICE_TYPE = new BleDeviceType(ECGMONITOR_UUID, ECGMONITOR_IMAGE, ECGMONITOR_NAME, ECGMONITOR_FACTORY);

    private EcgMonitorFactory(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public BleDevice createDevice(Context context) {
        return new EcgMonitorDevice(context, registerInfo);
    }

    @Override
    public BleFragment createFragment() {
        return BleFragment.create(registerInfo.getMacAddress(), EcgMonitorFragment.class);
    }
}