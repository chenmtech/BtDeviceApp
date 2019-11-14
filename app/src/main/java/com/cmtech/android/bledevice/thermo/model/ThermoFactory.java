package com.cmtech.android.bledevice.thermo.model;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.bledevice.thermo.view.ThermoFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.BleFragment;
import com.cmtech.android.bledeviceapp.model.BleDeviceType;
import com.cmtech.android.bledeviceapp.model.BleFactory;

public class ThermoFactory extends BleFactory {
    private static final String THERMOMETER_UUID = "aa30"; // 体温计
    private static final String THERMOMETER_DEFAULT_NAME = "体温计";
    private static final int THERMOMETER_DEFAULT_IMAGE_ID = R.drawable.ic_thermo_defaultimage;
    private static final String THERMOMETER_FACTORY = "com.cmtech.android.bledevice.thermo.model.ThermoFactory";

    public static final BleDeviceType THERMO_DEVICE_TYPE = new BleDeviceType(THERMOMETER_UUID, THERMOMETER_DEFAULT_IMAGE_ID, THERMOMETER_DEFAULT_NAME, THERMOMETER_FACTORY);

    private ThermoFactory(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public AbstractDevice createDevice() {
        return new ThermoDevice(registerInfo);
    }

    @Override
    public BleFragment createFragment() {
        return BleFragment.create(registerInfo.getMacAddress(), ThermoFragment.class);
    }
}
