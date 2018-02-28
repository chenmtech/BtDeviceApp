package com.cmtech.android.btdevice.common;

import com.cmtech.android.btdevice.unknown.UnknownDeviceFragment;
import com.cmtech.android.btdevice.thermo.ThermoFragment;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import java.util.Arrays;

/**
 * Created by bme on 2018/2/28.
 */

public class DeviceFragmentFactory {
    private static final String UUID_SIMPLE128GATTPROFILE = "0a20aa10-cce5-4025-a156-38ea833f6ef8";

    private static final String UUID_HEIGHTSCALE = "0a20aa20-cce5-4025-a156-38ea833f6ef8";

    private static final String UUID_THERMOMETER = "0a20aa30-cce5-4025-a156-38ea833f6ef8";

    private static final String UUID_ECGMONITOR = "0a20aa40-cce5-4025-a156-38ea833f6ef8";

    private static final String UUID_SIGGENERATOR = "0a20aa50-cce5-4025-a156-38ea833f6ef8";


    private DeviceFragmentFactory() {

    }

    public static DeviceFragment build(ConfiguredDevice device) {
        String uuid = device.getDeviceUuidInAd();
        if(uuid != null) {
            if(uuid.equals(UUID_THERMOMETER)) {
                return ThermoFragment.newInstance(device);
            } else if(uuid.equals(UUID_SIMPLE128GATTPROFILE)) {

            } else {
                return UnknownDeviceFragment.newInstance(device);
            }
        }
        return null;
    }
}
