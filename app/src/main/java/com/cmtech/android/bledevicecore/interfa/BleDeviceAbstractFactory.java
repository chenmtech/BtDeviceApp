package com.cmtech.android.bledevicecore.interfa;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorDeviceFactory;
import com.cmtech.android.bledevice.temphumid.TempHumidDeviceFactory;
import com.cmtech.android.bledevice.thermo.ThermoDeviceFactory;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledeviceapp.model.BleDeviceController;
import com.cmtech.android.bledeviceapp.model.BleDeviceFragment;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;

public abstract class BleDeviceAbstractFactory {
    // 支持的设备类型的16位UUID的字符串
    private static final String UUID_SIMPLE128GATTPROFILE = "aa10";
    private static final String UUID_HEIGHTSCALE = "aa20";
    private static final String UUID_THERMOMETER = "aa30";
    private static final String UUID_ECGMONITOR = "aa40";
    private static final String UUID_SIGGENERATOR = "aa50";
    private static final String UUID_TEMPHUMID = "aa60";
    private static final String UUID_UNKNOWN = "0000";

    public static BleDeviceAbstractFactory getBLEDeviceFactory(BleDeviceBasicInfo basicInfo) {
        String uuid = basicInfo.getUuidString();
        if(UUID_TEMPHUMID.equalsIgnoreCase(uuid))
            return new TempHumidDeviceFactory();
        else if(UUID_ECGMONITOR.equalsIgnoreCase(uuid))
            return new EcgMonitorDeviceFactory();
        else if(UUID_THERMOMETER.equalsIgnoreCase(uuid))
            return new ThermoDeviceFactory();
        else
            return null;
    }

    public abstract BleDevice createBleDevice(BleDeviceBasicInfo basicInfo);
    public abstract BleDeviceController createController(BleDevice device);
    public abstract BleDeviceFragment createFragment();
}
