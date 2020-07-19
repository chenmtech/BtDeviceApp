package com.cmtech.android.bledevice.ecg.webecg;

import android.content.Context;

import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;


public class WebEcgFactory extends DeviceFactory {
    private static final String UUID = "ab40"; // 设备支持的服务UUID短串
    private static final String DEFAULT_NAME = "网络心电"; // 缺省设备名
    private static final int DEFAULT_IMAGE_ID = R.drawable.ic_ecg_default_icon; // 缺省图标ID
    private static final String FACTORY = WebEcgFactory.class.getName(); // 工厂类名

    public static final DeviceType ECGWEBMONITOR_DEVICE_TYPE = new DeviceType(UUID, DEFAULT_IMAGE_ID, DEFAULT_NAME, FACTORY);

    private WebEcgFactory(DeviceCommonInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public IDevice createDevice(Context context) {
        return new WebEcgDevice(context, info);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(info.getAddress(), WebEcgFragment.class);
    }
}
