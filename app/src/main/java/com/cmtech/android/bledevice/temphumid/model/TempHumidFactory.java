package com.cmtech.android.bledevice.temphumid.model;

import android.content.Context;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.BleDeviceType;
import com.cmtech.android.bledevice.temphumid.view.TempHumidFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.BleFragment;
import com.cmtech.android.bledeviceapp.model.BleFactory;

// 会根据设备类型BleDeviceType，通过反射创建工厂类实例
public class TempHumidFactory extends BleFactory {
    private static final String TEMPHUMID_UUID = "aa60"; // 温湿度计
    private static final String TEMPHUMID_NAME = "温湿度计";
    private static final int TEMPHUMID_IMAGE = R.drawable.ic_temphumid_defaultimage;
    private static final String TEMPHUMID_FACTORY = "com.cmtech.android.bledevice.temphumid.model.TempHumidFactory";

    public static final BleDeviceType TEMPHUMID_DEVICE_TYPE = new BleDeviceType(TEMPHUMID_UUID, TEMPHUMID_IMAGE, TEMPHUMID_NAME, TEMPHUMID_FACTORY);

    private TempHumidFactory(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public BleDevice createDevice(Context context) {
        return new TempHumidDevice(context, registerInfo);
    }

    @Override
    public BleFragment createFragment() {
        return BleFragment.create(registerInfo.getMacAddress(), TempHumidFragment.class);
    }


}
