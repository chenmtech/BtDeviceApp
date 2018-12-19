package com.cmtech.android.bledevice;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.BleDeviceType;

/*
 * SupportedDeviceType：支持的设备类型
 * Created by bme on 2018/10/13.
 */

public class SupportedDeviceType {

    //支持的设备类型信息，以后要保存到数据库中，并从数据库中加载

    // 支持的设备类型的16位UUID的字符串
    private static final String UUID_SIMPLE128GATTPROFILE       = "aa10";       // 简单GATT例程
    private static final String UUID_HEIGHTSCALE                = "aa20";       // 高度计
    private static final String UUID_THERMOMETER                = "aa30";       // 体温计
    private static final String UUID_ECGMONITOR                 = "aa40";       // 心电监护仪
    private static final String UUID_SIGGENERATOR               = "aa50";       // 信号发生器
    private static final String UUID_TEMPHUMID                  = "aa60";       // 温湿度计
    private static final String UUID_UNKNOWN                    = "0000";       // 未知设备

    // 支持的设备类型的缺省名称
    private static final String NAME_SIMPLE128GATTPROFILE        = "简单GATT例程";
    private static final String NAME_HEIGHTSCALE                 = "高度计";
    private static final String NAME_THERMOMETER                 = "体温计";
    private static final String NAME_ECGMONITOR                  = "心电带";
    private static final String NAME_SIGGENERATOR                = "信号发生器";
    private static final String NAME_TEMPHUMID                   = "温湿度计";
    private static final String NAME_UNKNOWN                     = "未知设备";

    // 支持的设备类型的缺省图标
    private static final int IMAGE_SIMPLE128GATTPROFILE        = R.drawable.ic_unknowndevice_defaultimage;
    private static final int IMAGE_HEIGHTSCALE                 = R.drawable.ic_unknowndevice_defaultimage;
    private static final int IMAGE_THERMOMETER                 = R.drawable.ic_thermo_defaultimage;
    private static final int IMAGE_ECGMONITOR                  = R.drawable.ic_ecgmonitor_defaultimage;
    private static final int IMAGE_SIGGENERATOR                = R.drawable.ic_unknowndevice_defaultimage;
    private static final int IMAGE_TEMPHUMID                   = R.drawable.ic_temphumid_defaultimage;
    private static final int IMAGE_UNKNOWN                     = R.drawable.ic_unknowndevice_defaultimage;

    // 支持的设备类型对应的抽象工厂类的完整名称
    private static final String thermoFactory = "com.cmtech.android.bledevice.thermo.model.ThermoDeviceFactory";
    private static final String ecgMonitorFactory = "com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDeviceFactory";
    private static final String tempHumidDeviceFactory = "com.cmtech.android.bledevice.temphumid.model.TempHumidDeviceFactory";
    public  static final String unknownDeviceFactory = "";

    // 支持的设备类型,目前支持三种设备
    // 体温计
    private static final BleDeviceType DEVTYPE_THERMOMETER =
            new BleDeviceType(UUID_THERMOMETER, IMAGE_THERMOMETER, NAME_THERMOMETER, thermoFactory);
    // 心电带
    private static final BleDeviceType DEVTYPE_ECGMONITOR =
            new BleDeviceType(UUID_ECGMONITOR, IMAGE_ECGMONITOR, NAME_ECGMONITOR, ecgMonitorFactory);
    // 温湿度计
    private static final BleDeviceType DEVTYPE_TEMPHUMID =
            new BleDeviceType(UUID_TEMPHUMID, IMAGE_TEMPHUMID, NAME_TEMPHUMID, tempHumidDeviceFactory);
    // 未知设备
    private static final BleDeviceType DEVTYPE_UNKNOWN =
            new BleDeviceType(UUID_UNKNOWN, IMAGE_UNKNOWN, NAME_UNKNOWN, unknownDeviceFactory);

    // 通过UUID获取对应的设备类型
    public static BleDeviceType getDeviceTypeFromUuid(String uuid) {
        if(DEVTYPE_THERMOMETER.getUuid().equalsIgnoreCase(uuid)) return DEVTYPE_THERMOMETER;
        if(DEVTYPE_ECGMONITOR.getUuid().equalsIgnoreCase(uuid)) return DEVTYPE_ECGMONITOR;
        if(DEVTYPE_TEMPHUMID.getUuid().equalsIgnoreCase(uuid)) return DEVTYPE_TEMPHUMID;
        return DEVTYPE_UNKNOWN;
    }
}
