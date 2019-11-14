package com.cmtech.android.bledevice.ecgwebmonitor;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.BleFragment;
import com.cmtech.android.bledeviceapp.model.BleDeviceType;
import com.cmtech.android.bledeviceapp.model.BleFactory;


public class EcgWebMonitorFactory extends BleFactory {
    private static final String UUID = "ab40"; // 设备支持的服务UUID短串
    private static final String DEFAULT_NAME = "心电广播"; // 缺省设备名
    private static final int DEFAULT_IMAGE_ID = R.drawable.ic_ecgmonitor_default_image; // 缺省图标ID
    private static final String FACTORY = EcgWebMonitorFactory.class.getName(); // 工厂类名

    public static final BleDeviceType ECGWEBMONITOR_DEVICE_TYPE = new BleDeviceType(UUID, DEFAULT_IMAGE_ID, DEFAULT_NAME, FACTORY);

    private EcgWebMonitorFactory(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public AbstractDevice createDevice() {
        return new EcgWebMonitorDevice(registerInfo);
    }

    @Override
    public BleFragment createFragment() {
        return BleFragment.create(registerInfo.getMacAddress(), EcgWebMonitorFragment.class);
    }
}
