package com.cmtech.android.bledevice.ecg.webecg;

import com.cmtech.android.ble.core.DeviceInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;


public class WebEcgFactory extends DeviceFactory {
    private static final String UUID = "ab40"; // 设备支持的服务UUID短串
    private static final String DEFAULT_NAME = "网络心电"; // 缺省设备名
    private static final int DEFAULT_IMAGE_ID = R.drawable.ic_ecgmonitor_default_image; // 缺省图标ID
    private static final String FACTORY = WebEcgFactory.class.getName(); // 工厂类名

    public static final DeviceType ECGWEBMONITOR_DEVICE_TYPE = new DeviceType(UUID, DEFAULT_IMAGE_ID, DEFAULT_NAME, FACTORY);

    private WebEcgFactory(DeviceInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public IDevice createDevice() {
        return new WebEcgDevice(registerInfo);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(registerInfo.getAddress(), WebEcgFragment.class);
    }
}
