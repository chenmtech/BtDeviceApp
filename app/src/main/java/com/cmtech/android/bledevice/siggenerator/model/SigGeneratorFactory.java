package com.cmtech.android.bledevice.siggenerator.model;

import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.siggenerator.view.SigGeneratorFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;

public class SigGeneratorFactory extends DeviceFactory {
    private static final String SIGGENERATOR_UUID = "aa50"; // 设备支持的服务UUID短串
    private static final String SIGGENERATOR_DEFAULT_NAME = "信号发生器";
    private static final int SIGGENERATOR_DEFAULT_IMAGE_ID = R.drawable.ic_siggenerator_defaultimage;
    private static final String SIGGENERATOR_FACTORY = "com.cmtech.android.bledevice.siggenerator.model.SigGeneratorFactory";

    public static final DeviceType SIGGENERATOR_DEVICE_TYPE = new DeviceType(SIGGENERATOR_UUID, SIGGENERATOR_DEFAULT_IMAGE_ID, SIGGENERATOR_DEFAULT_NAME, SIGGENERATOR_FACTORY);

    private SigGeneratorFactory(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public IDevice createDevice() {
        return SigGeneratorDevice.create(registerInfo);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(registerInfo.getMacAddress(), SigGeneratorFragment.class);
    }
}
