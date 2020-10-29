package com.cmtech.android.bledevice.thermo.model;

import android.content.Context;

import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.thermo.activityfragment.ThermoFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.fragment.DeviceFragment;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;

public class ThermoFactory extends DeviceFactory {
    private static final String THERMO_UUID = "1809"; // thermometer uuid
    private static final String THERMO_DEFAULT_NAME = MyApplication.getStr(R.string.thermo_monitor_name);
    private static final int THERMO_DEFAULT_ICON = R.drawable.ic_thermo_default_icon;
    private static final String THERMO_FACTORY = ThermoFactory.class.getName();

    public static final DeviceType THERMO_DEVICE_TYPE = new DeviceType(THERMO_UUID, THERMO_DEFAULT_ICON, THERMO_DEFAULT_NAME, THERMO_FACTORY);

    private ThermoFactory(DeviceCommonInfo info) {
        super(info);
    }

    @Override
    public IDevice createDevice(Context context) {
        return new ThermoDevice(context, info);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(info.getAddress(), ThermoFragment.class);
    }
}
