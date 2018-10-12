package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorDeviceFactory;
import com.cmtech.android.bledevice.temphumid.TempHumidDeviceFactory;
import com.cmtech.android.bledevice.thermo.ThermoDeviceFactory;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;

/*
 * BleDeviceAbstractFactory：Ble设备抽象工厂
 * Created by bme on 2018/10/13.
 */
public abstract class BleDeviceAbstractFactory {
    // 支持的设备类型的16位UUID的字符串
    private static final String UUID_SIMPLE128GATTPROFILE   = "aa10";
    private static final String UUID_HEIGHTSCALE            = "aa20";       // 高度计
    private static final String UUID_THERMOMETER            = "aa30";       // 体温计
    private static final String UUID_ECGMONITOR             = "aa40";       // 心电监护仪
    private static final String UUID_SIGGENERATOR           = "aa50";       // 信号发生器
    private static final String UUID_TEMPHUMID              = "aa60";       // 温湿度计
    private static final String UUID_UNKNOWN                = "0000";       // 未知设备


    // 获取BleDevice的抽象工厂
    public static BleDeviceAbstractFactory getBLEDeviceFactory(BleDevice device) {
        return (device == null) ? null : getBLEDeviceFactory(device.getBasicInfo());
    }

    public static BleDeviceAbstractFactory getBLEDeviceFactory(BleDeviceBasicInfo basicInfo) {
        return (basicInfo == null) ? null : getBLEDeviceFactory(basicInfo.getUuidString());
    }

    public static BleDeviceAbstractFactory getBLEDeviceFactory(String uuidString) {
        if(UUID_TEMPHUMID.equalsIgnoreCase(uuidString))
            return new TempHumidDeviceFactory();
        else if(UUID_ECGMONITOR.equalsIgnoreCase(uuidString))
            return new EcgMonitorDeviceFactory();
        else if(UUID_THERMOMETER.equalsIgnoreCase(uuidString))
            return new ThermoDeviceFactory();
        else
            return null;
    }

    // 创建BleDevice
    public abstract BleDevice createBleDevice(BleDeviceBasicInfo basicInfo);
    // 创建控制器
    public abstract BleDeviceController createController(BleDevice device);
    // 创建Fragment
    public abstract BleDeviceFragment createFragment();
}
