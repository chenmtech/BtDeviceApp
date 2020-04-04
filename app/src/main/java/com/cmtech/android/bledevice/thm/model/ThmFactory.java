package com.cmtech.android.bledevice.thm.model;

import com.cmtech.android.ble.core.DeviceInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.thm.view.ThmFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;

// 会根据设备类型BleDeviceType，通过反射创建工厂类实例
public class ThmFactory extends DeviceFactory {
    private static final String THM_UUID = "aa60"; // temp & humid monitor uuid
    private static final String THM_DEFAULT_NAME = "康明温湿度计";
    private static final int THM_DEFAULT_ICON = R.drawable.ic_thm_default_icon;
    private static final String THM_FACTORY = ThmFactory.class.getName();

    public static final DeviceType THM_DEVICE_TYPE = new DeviceType(THM_UUID, THM_DEFAULT_ICON, THM_DEFAULT_NAME, THM_FACTORY);

    private ThmFactory(DeviceInfo info) {
        super(info);
    }

    @Override
    public IDevice createDevice() {
        return new ThmDevice(info);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(info.getAddress(), ThmFragment.class);
    }

}
