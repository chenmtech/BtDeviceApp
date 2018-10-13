package com.cmtech.android.bledevicecore.model;

import com.cmtech.android.bledeviceapp.R;

/**
 * BleDeviceType：Ble设备类型
 * Created by bme on 2018/10/13.
 */

public class BleDeviceType {

    //支持的设备类型信息，以后要保存到数据库中，并从数据库中加载

    // 支持的设备类型的16位UUID的字符串
    private static final String UUID_SIMPLE128GATTPROFILE      = "aa10";
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


    public static final BleDeviceType DEVTYPE_THERMOMETER =
            new BleDeviceType(UUID_THERMOMETER, IMAGE_THERMOMETER, NAME_THERMOMETER);
    public static final BleDeviceType DEVTYPE_ECGMONITOR =
            new BleDeviceType(UUID_ECGMONITOR, IMAGE_ECGMONITOR, NAME_ECGMONITOR);
    public static final BleDeviceType DEVTYPE_TEMPHUMID =
            new BleDeviceType(UUID_TEMPHUMID, IMAGE_TEMPHUMID, NAME_TEMPHUMID);
    public static final BleDeviceType DEVTYPE_UNKNOWN =
            new BleDeviceType(UUID_UNKNOWN, IMAGE_UNKNOWN, NAME_UNKNOWN);

    private String uuid;                  // 设备16位UUID字符串
    private int defaultImage;           // 缺省图标
    private String defaultNickname;     // 缺省设备名

    private BleDeviceType(String uuid, int defaultImage, String defaultNickname) {
        this.uuid = uuid;
        this.defaultImage = defaultImage;
        this.defaultNickname = defaultNickname;
    }

    public int getDefaultImage() {
        return defaultImage;
    }

    public String getDefaultNickname() {
        return defaultNickname;
    }


    public static BleDeviceType fromUuid(String uuid) {
        if(DEVTYPE_THERMOMETER.uuid.equalsIgnoreCase(uuid)) return DEVTYPE_THERMOMETER;
        if(DEVTYPE_ECGMONITOR.uuid.equalsIgnoreCase(uuid)) return DEVTYPE_ECGMONITOR;
        if(DEVTYPE_TEMPHUMID.uuid.equalsIgnoreCase(uuid)) return DEVTYPE_TEMPHUMID;
        return DEVTYPE_UNKNOWN;
    }
}
