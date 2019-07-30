package com.cmtech.android.bledevice.siggenerator.model;

import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceType;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgMonitorFragment;
import com.cmtech.android.bledevice.siggenerator.view.SigGeneratorFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.BleDeviceFragment;
import com.cmtech.android.bledeviceapp.model.AbstractBleDeviceFactory;

public class SigGeneratorDeviceFactory extends AbstractBleDeviceFactory {
    private static final String UUID_SIGGENERATOR                 = "aa50";       // 信号发生器

    private static final String NAME_SIGGENERATOR                  = "信号发生器";

    private static final int IMAGE_SIGGENERATOR                  = R.drawable.ic_siggenerator_defaultimage;

    private static final String sigGeneratorFactory = "com.cmtech.android.bledevice.siggenerator.model.SigGeneratorDeviceFactory";

    public static final BleDeviceType SIGGENERATOR_DEVICE_TYPE = new BleDeviceType(UUID_SIGGENERATOR, IMAGE_SIGGENERATOR, NAME_SIGGENERATOR, sigGeneratorFactory);

    @Override
    public BleDevice createDevice() {
        return new SigGeneratorDevice(basicInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return BleDeviceFragment.create(basicInfo.getMacAddress(), SigGeneratorFragment.class);
    }
}
