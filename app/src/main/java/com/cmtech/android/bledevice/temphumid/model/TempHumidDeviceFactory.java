package com.cmtech.android.bledevice.temphumid.model;

import android.content.Context;

import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceType;
import com.cmtech.android.bledevice.temphumid.view.TempHumidFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.BleDeviceFragment;
import com.cmtech.android.bledeviceapp.model.AbstractBleDeviceFactory;

// 会根据设备类型BleDeviceType，通过反射创建工厂类实例
public class TempHumidDeviceFactory extends AbstractBleDeviceFactory {
    private static final String UUID_TEMPHUMID                  = "aa60";       // 温湿度计

    private static final String NAME_TEMPHUMID                   = "温湿度计";

    private static final int IMAGE_TEMPHUMID                   = R.drawable.ic_temphumid_defaultimage;

    private static final String tempHumidDeviceFactory = "com.cmtech.android.bledevice.temphumid.model.TempHumidDeviceFactory";

    public static final BleDeviceType TEMPHUMID_DEVICE_TYPE = new BleDeviceType(UUID_TEMPHUMID, IMAGE_TEMPHUMID, NAME_TEMPHUMID, tempHumidDeviceFactory);


    @Override
    public BleDevice createDevice(Context context) {
        return new TempHumidDevice(context, basicInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return BleDeviceFragment.create(basicInfo.getMacAddress(), TempHumidFragment.class);
    }


}
