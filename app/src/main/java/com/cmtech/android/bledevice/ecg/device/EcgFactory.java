package com.cmtech.android.bledevice.ecg.device;

import android.content.Context;

import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.ecg.fragment.EcgFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;

// 根据设备类型BleDeviceType，通过反射创建工厂类实例
public class EcgFactory extends DeviceFactory {
    private static final String ECG_UUID = "aa40"; // ecg uuid string
    private static final String ECG_DEFAULT_NAME = "康明心电带"; // default device name
    private static final int ECG_DEFAULT_ICON = R.drawable.ic_ecg_default_icon; // default device icon id
    private static final String ECG_FACTORY = EcgFactory.class.getName();// factory class name

    public static final DeviceType ECG_DEVICE_TYPE = new DeviceType(ECG_UUID, ECG_DEFAULT_ICON, ECG_DEFAULT_NAME, ECG_FACTORY);

    private EcgFactory(DeviceCommonInfo info) {
        super(info);
    }

    @Override
    public IDevice createDevice(Context context) {
        return new EcgDevice(context, info);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(info.getAddress(), EcgFragment.class);
    }
}
