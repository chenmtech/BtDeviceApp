package com.cmtech.android.btdeviceapp.interfa;

import com.cmtech.android.btdevice.ecgmonitor.EcgMonitorDeviceFactory;
import com.cmtech.android.btdevice.temphumid.TempHumidDeviceFactory;
import com.cmtech.android.btdevice.thermo.ThermoDeviceFactory;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;
import com.cmtech.android.btdeviceapp.model.BLEDevicePersistantInfo;
import com.cmtech.android.btdeviceapp.model.BLEDeviceType;

public abstract class BLEDeviceAbstractFactory {
    // 支持的设备类型的16位UUID的字符串
    private static final String UUID_SIMPLE128GATTPROFILE = "aa10";
    private static final String UUID_HEIGHTSCALE = "aa20";
    private static final String UUID_THERMOMETER = "aa30";
    private static final String UUID_ECGMONITOR = "aa40";
    private static final String UUID_SIGGENERATOR = "aa50";
    private static final String UUID_TEMPHUMID = "aa60";
    private static final String UUID_UNKNOWN = "0000";

    public static BLEDeviceAbstractFactory getBLEDeviceFactory(BLEDevicePersistantInfo persistantInfo) {
        String uuid = persistantInfo.getUuidString();
        if(UUID_TEMPHUMID.equalsIgnoreCase(uuid))
            return new TempHumidDeviceFactory();
        else if(UUID_ECGMONITOR.equalsIgnoreCase(uuid))
            return new EcgMonitorDeviceFactory();
        else if(UUID_THERMOMETER.equalsIgnoreCase(uuid))
            return new ThermoDeviceFactory();
        else
            return null;
    }

    public abstract BLEDeviceModel createDevice(BLEDevicePersistantInfo persistantInfo);
    public abstract BLEDeviceController createController(BLEDeviceModel device, MainActivity activity);
    public abstract BLEDeviceFragment createFragment();
}
