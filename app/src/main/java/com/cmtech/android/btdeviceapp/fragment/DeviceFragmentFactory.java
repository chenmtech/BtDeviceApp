package com.cmtech.android.btdeviceapp.fragment;

import com.cmtech.android.btdevice.temphumid.TempHumidFragment;
import com.cmtech.android.btdevice.unknown.UnknownDeviceFragment;
import com.cmtech.android.btdevice.thermo.ThermoFragment;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;
import com.cmtech.android.btdeviceapp.util.Uuid;

import java.util.UUID;

/**
 * Created by bme on 2018/2/28.
 */

public class DeviceFragmentFactory {
    private static final String UUID_SIMPLE128GATTPROFILE = "aa10";

    private static final String UUID_HEIGHTSCALE = "aa20";

    private static final String UUID_THERMOMETER = "aa30";

    private static final String UUID_ECGMONITOR = "aa40";

    private static final String UUID_SIGGENERATOR = "aa50";

    private static final String UUID_TEMPHUMID = "aa60";


    private DeviceFragmentFactory() {

    }

    public static DeviceFragment build(ConfiguredDevice device) {
        UUID uuid = device.getDeviceUuidInAd();
        if(uuid != null) {
            if(uuid.equals(Uuid.shortStringToUuid(UUID_THERMOMETER))) {
                return ThermoFragment.newInstance();
            } else if(uuid.equals(Uuid.shortStringToUuid(UUID_TEMPHUMID))) {
                return TempHumidFragment.newInstance();
            } else {
                return UnknownDeviceFragment.newInstance();
            }
        }
        return null;
    }
}
