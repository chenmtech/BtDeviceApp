package com.cmtech.android.bledevice;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceType;

public class SupportedDeviceType {

    //支持的设备类型信息，以后要保存到数据库中，并从数据库中加载

    // 支持的设备类型的16位UUID的字符串
    private static final String UUID_SIMPLE128GATTPROFILE      = "aa10";       // 简单GATT例程
    private static final String UUID_HEIGHTSCALE                = "aa20";       // 高度计
    private static final String UUID_THERMOMETER                = "aa30";       // 体温计
    private static final String UUID_ECGMONITOR                 = "aa40";       // 心电监护仪
    private static final String UUID_SIGGENERATOR               = "aa50";       // 信号发生器
    private static final String UUID_TEMPHUMID                  = "aa60";       // 温湿度计
    private static final String UUID_UNKNOWN                    = "0000";       // 未知设备

    // 支持的设备类型的缺省名称
    private static final String NAME_SIMPLE128GATTPROFILE       = "简单GATT例程";
    private static final String NAME_HEIGHTSCALE                 = "高度计";
    private static final String NAME_THERMOMETER                 = "体温计";
    private static final String NAME_ECGMONITOR                  = "心电监护仪";
    private static final String NAME_SIGGENERATOR                = "信号发生器";
    private static final String NAME_TEMPHUMID                   = "温湿度计";
    private static final String NAME_UNKNOWN                     = "未知设备";

    // 支持的设备类型的缺省图标
    private static final int IMAGE_SIMPLE128GATTPROFILE       = R.mipmap.ic_unknown_128px;
    private static final int IMAGE_HEIGHTSCALE                 = R.mipmap.ic_unknown_128px;
    private static final int IMAGE_THERMOMETER                 = R.drawable.thermo_image;
    private static final int IMAGE_ECGMONITOR                  = R.drawable.ecgmonitor_image;
    private static final int IMAGE_SIGGENERATOR                = R.mipmap.ic_unknown_128px;
    private static final int IMAGE_TEMPHUMID                   = R.drawable.temphumid_image;
    private static final int IMAGE_UNKNOWN                     = R.mipmap.ic_unknown_128px;

    // 支持的设备类型对应的抽象工厂类的完整名称
    private static final String thermoFactory = "com.cmtech.android.bledevice.thermo.ThermoDeviceFactory";
    private static final String ecgMonitorFactory = "com.cmtech.android.bledevice.ecgmonitor.EcgMonitorDeviceFactory";
    private static final String tempHumidDeviceFactory = "com.cmtech.android.bledevice.temphumid.TempHumidDeviceFactory";
    private static final String unknownDeviceFactory = "";

    // 支持的设备类型
    public static final BleDeviceType DEVTYPE_THERMOMETER =
            new BleDeviceType(UUID_THERMOMETER, IMAGE_THERMOMETER, NAME_THERMOMETER, thermoFactory);
    public static final BleDeviceType DEVTYPE_ECGMONITOR =
            new BleDeviceType(UUID_ECGMONITOR, IMAGE_ECGMONITOR, NAME_ECGMONITOR, ecgMonitorFactory);
    public static final BleDeviceType DEVTYPE_TEMPHUMID =
            new BleDeviceType(UUID_TEMPHUMID, IMAGE_TEMPHUMID, NAME_TEMPHUMID, tempHumidDeviceFactory);
    public static final BleDeviceType DEVTYPE_UNKNOWN =
            new BleDeviceType(UUID_UNKNOWN, IMAGE_UNKNOWN, NAME_UNKNOWN, unknownDeviceFactory);

    // 通过UUID获取对应的设备类型
    public static BleDeviceType getDeviceTypeFromUuid(String uuid) {
        if(DEVTYPE_THERMOMETER.getUuid().equalsIgnoreCase(uuid)) return DEVTYPE_THERMOMETER;
        if(DEVTYPE_ECGMONITOR.getUuid().equalsIgnoreCase(uuid)) return DEVTYPE_ECGMONITOR;
        if(DEVTYPE_TEMPHUMID.getUuid().equalsIgnoreCase(uuid)) return DEVTYPE_TEMPHUMID;
        return DEVTYPE_UNKNOWN;
    }
}