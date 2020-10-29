package com.cmtech.android.bledevice.sgg.model;

import android.content.Context;

import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.sgg.view.SigGeneratorFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.fragment.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;

public class SigGeneratorFactory extends DeviceFactory {
    private static final String SGG_UUID = "aa50"; // 设备支持的服务UUID短串
    private static final String SGG_DEFAULT_NAME = "信号发生器";
    private static final int SGG_DEFAULT_ICON = R.drawable.ic_sgg_default_icon;
    private static final String SGG_FACTORY = SigGeneratorFactory.class.getName();

    public static final DeviceType SGG_DEVICE_TYPE = new DeviceType(SGG_UUID, SGG_DEFAULT_ICON, SGG_DEFAULT_NAME, SGG_FACTORY);

    private SigGeneratorFactory(DeviceCommonInfo info) {
        super(info);
    }

    @Override
    public IDevice createDevice(Context context) {
        return new SigGeneratorDevice(context, info);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(info.getAddress(), SigGeneratorFragment.class);
    }
}
