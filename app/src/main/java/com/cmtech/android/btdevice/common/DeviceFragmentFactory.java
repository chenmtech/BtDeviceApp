package com.cmtech.android.btdevice.common;

import com.cmtech.android.btdevice.unknown.UnknownDeviceFragment;
import com.cmtech.android.btdevice.thermo.ThermoFragment;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

/**
 * Created by bme on 2018/2/28.
 */

public class DeviceFragmentFactory {
    private static final String UUID_SIMPLE128GATTPROFILE = "aa10";

    private static final String UUID_HEIGHTSCALE = "aa20";

    private static final String UUID_THERMOMETER = "aa30";

    private static final String UUID_ECGMONITOR = "aa40";

    private static final String UUID_SIGGENERATOR = "aa50";


    private DeviceFragmentFactory() {

    }

    public static DeviceFragment build(ConfiguredDevice device) {
        String uuid = device.getDeviceUuidInAd();
        if(uuid != null) {
            if(uuid.equalsIgnoreCase(Uuid.from16To128(UUID_THERMOMETER))) {
                return ThermoFragment.newInstance();
            } else if(uuid.equalsIgnoreCase(Uuid.from16To128(UUID_SIMPLE128GATTPROFILE))) {

            } else {
                return UnknownDeviceFragment.newInstance();
            }
        }
        return null;
    }
}
