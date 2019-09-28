package com.cmtech.android.bledevice.thermo.model;

import android.content.Context;

import com.cmtech.android.ble.extend.BleDeviceRegisterInfo;
import com.cmtech.android.bledeviceapp.model.BleDeviceFactory;
import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.bledeviceapp.activity.BleDeviceFragment;
import com.cmtech.android.ble.extend.BleDeviceType;
import com.cmtech.android.bledevice.thermo.view.ThermoFragment;
import com.cmtech.android.bledeviceapp.R;

public class ThermoDeviceFactory extends BleDeviceFactory {
    private static final String UUID_THERMOMETER                = "aa30";       // 体温计

    private static final String NAME_THERMOMETER                 = "体温计";

    private static final int IMAGE_THERMOMETER                 = R.drawable.ic_thermo_defaultimage;

    private static final String thermoFactory = "com.cmtech.android.bledevice.thermo.model.ThermoDeviceFactory";

    public static final BleDeviceType THERMO_DEVICE_TYPE = new BleDeviceType(UUID_THERMOMETER, IMAGE_THERMOMETER, NAME_THERMOMETER, thermoFactory);

    private ThermoDeviceFactory(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public BleDevice createDevice(Context context) {
        return new ThermoDevice(context, registerInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return BleDeviceFragment.create(registerInfo.getMacAddress(), ThermoFragment.class);
    }
}
