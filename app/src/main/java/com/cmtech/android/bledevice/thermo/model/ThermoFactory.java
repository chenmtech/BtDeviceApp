package com.cmtech.android.bledevice.thermo.model;

import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.thermo.view.ThermoFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;

public class ThermoFactory extends DeviceFactory {
    private static final String THERMOMETER_UUID = "1809"; // thermometer
    private static final String THERMOMETER_DEFAULT_NAME = "标准体温计";
    private static final int THERMOMETER_DEFAULT_IMAGE_ID = R.drawable.ic_thermo_defaultimage;
    private static final String THERMOMETER_FACTORY = ThermoFactory.class.getName();

    public static final DeviceType THERMO_DEVICE_TYPE = new DeviceType(THERMOMETER_UUID, THERMOMETER_DEFAULT_IMAGE_ID, THERMOMETER_DEFAULT_NAME, THERMOMETER_FACTORY);

    private ThermoFactory(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public IDevice createDevice() {
        return new ThermoDevice(registerInfo);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(registerInfo.getAddress(), ThermoFragment.class);
    }
}
