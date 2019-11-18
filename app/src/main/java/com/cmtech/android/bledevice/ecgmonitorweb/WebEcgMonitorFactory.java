package com.cmtech.android.bledevice.ecgmonitorweb;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;


public class WebEcgMonitorFactory extends DeviceFactory {
    private static final String UUID = "ab40"; // 设备支持的服务UUID短串
    private static final String DEFAULT_NAME = "心电广播"; // 缺省设备名
    private static final int DEFAULT_IMAGE_ID = R.drawable.ic_ecgmonitor_default_image; // 缺省图标ID
    private static final String FACTORY = WebEcgMonitorFactory.class.getName(); // 工厂类名

    public static final DeviceType ECGWEBMONITOR_DEVICE_TYPE = new DeviceType(UUID, DEFAULT_IMAGE_ID, DEFAULT_NAME, FACTORY);

    private WebEcgMonitorFactory(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public AbstractDevice createDevice() {
        return new WebEcgMonitorDevice(registerInfo);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(registerInfo.getMacAddress(), WebEcgMonitorFragment.class);
    }
}
