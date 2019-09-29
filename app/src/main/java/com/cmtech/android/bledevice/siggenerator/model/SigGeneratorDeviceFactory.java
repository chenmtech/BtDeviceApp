package com.cmtech.android.bledevice.siggenerator.model;

import android.content.Context;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.BleDeviceType;
import com.cmtech.android.bledevice.siggenerator.view.SigGeneratorFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.BleDeviceFragment;
import com.cmtech.android.bledeviceapp.model.BleDeviceFactory;

public class SigGeneratorDeviceFactory extends BleDeviceFactory {
    private static final String SIGGENERATOR_UUID = "aa50"; // 信号发生器
    private static final String SIGGENERATOR_NAME = "信号发生器";
    private static final int SIGGENERATOR_IMAGE = R.drawable.ic_siggenerator_defaultimage;
    private static final String SIGGENERATOR_FACTORY = "com.cmtech.android.bledevice.siggenerator.model.SigGeneratorDeviceFactory";

    public static final BleDeviceType SIGGENERATOR_DEVICE_TYPE = new BleDeviceType(SIGGENERATOR_UUID, SIGGENERATOR_IMAGE, SIGGENERATOR_NAME, SIGGENERATOR_FACTORY);

    private SigGeneratorDeviceFactory(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public BleDevice createDevice(Context context) {
        return new SigGeneratorDevice(context, registerInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return BleDeviceFragment.create(registerInfo.getMacAddress(), SigGeneratorFragment.class);
    }
}
