package com.cmtech.android.bledevice.thermo.model;

import com.cmtech.android.bledeviceapp.model.AbstractBleDeviceFactory;
import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.bledeviceapp.activity.BleDeviceFragment;
import com.cmtech.android.ble.extend.BleDeviceType;
import com.cmtech.android.bledevice.thermo.view.ThermoFragment;
import com.cmtech.android.bledeviceapp.R;

public class ThermoDeviceFactory extends AbstractBleDeviceFactory {
    private static final String UUID_THERMOMETER                = "aa30";       // 体温计

    private static final String NAME_THERMOMETER                 = "体温计";

    private static final int IMAGE_THERMOMETER                 = R.drawable.ic_thermo_defaultimage;

    private static final String thermoFactory = "com.cmtech.android.bledevice.thermo.model.ThermoDeviceFactory";

    public static final BleDeviceType THERMO_DEVICE_TYPE = new BleDeviceType(UUID_THERMOMETER, IMAGE_THERMOMETER, NAME_THERMOMETER, thermoFactory);


    @Override
    public BleDevice createDevice() {
        return new ThermoDevice(basicInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return BleDeviceFragment.create(basicInfo.getMacAddress(), ThermoFragment.class);
    }
}
