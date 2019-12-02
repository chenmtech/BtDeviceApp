package com.cmtech.android.bledevice.temphumid.model;

import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.temphumid.view.TempHumidFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;

// 会根据设备类型BleDeviceType，通过反射创建工厂类实例
public class TempHumidFactory extends DeviceFactory {
    private static final String TEMPHUMID_UUID = "aa60"; // 温湿度计
    private static final String TEMPHUMID_DEFAULT_NAME = "温湿度计";
    private static final int TEMPHUMID_DEFAULT_IMAGE_ID = R.drawable.ic_temphumid_defaultimage;
    private static final String TEMPHUMID_FACTORY = "com.cmtech.android.bledevice.temphumid.model.TempHumidFactory";

    public static final DeviceType TEMPHUMID_DEVICE_TYPE = new DeviceType(TEMPHUMID_UUID, TEMPHUMID_DEFAULT_IMAGE_ID, TEMPHUMID_DEFAULT_NAME, TEMPHUMID_FACTORY);

    private TempHumidFactory(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public IDevice createDevice() {
        return new TempHumidDevice(registerInfo);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(registerInfo.getMacAddress(), TempHumidFragment.class);
    }


}
