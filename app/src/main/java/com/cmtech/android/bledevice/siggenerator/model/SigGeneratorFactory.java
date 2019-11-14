package com.cmtech.android.bledevice.siggenerator.model;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.bledevice.siggenerator.view.SigGeneratorFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.BleFragment;
import com.cmtech.android.bledeviceapp.model.BleDeviceType;
import com.cmtech.android.bledeviceapp.model.BleFactory;

public class SigGeneratorFactory extends BleFactory {
    private static final String SIGGENERATOR_UUID = "aa50"; // 设备支持的服务UUID短串
    private static final String SIGGENERATOR_DEFAULT_NAME = "信号发生器";
    private static final int SIGGENERATOR_DEFAULT_IMAGE_ID = R.drawable.ic_siggenerator_defaultimage;
    private static final String SIGGENERATOR_FACTORY = "com.cmtech.android.bledevice.siggenerator.model.SigGeneratorFactory";

    public static final BleDeviceType SIGGENERATOR_DEVICE_TYPE = new BleDeviceType(SIGGENERATOR_UUID, SIGGENERATOR_DEFAULT_IMAGE_ID, SIGGENERATOR_DEFAULT_NAME, SIGGENERATOR_FACTORY);

    private SigGeneratorFactory(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public AbstractDevice createDevice() {
        return new SigGeneratorDevice(registerInfo);
    }

    @Override
    public BleFragment createFragment() {
        return BleFragment.create(registerInfo.getMacAddress(), SigGeneratorFragment.class);
    }
}
