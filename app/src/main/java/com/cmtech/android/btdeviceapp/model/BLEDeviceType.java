package com.cmtech.android.btdeviceapp.model;

import com.cmtech.android.btdeviceapp.R;

public class BLEDeviceType {

    //支持的设备类型信息，以后要保存到数据库中，并从数据库中加载

    // 支持的设备类型的16位UUID的字符串
    private static final String UUID_SIMPLE128GATTPROFILE = "aa10";
    private static final String UUID_HEIGHTSCALE = "aa20";
    private static final String UUID_THERMOMETER = "aa30";
    private static final String UUID_ECGMONITOR = "aa40";
    private static final String UUID_SIGGENERATOR = "aa50";
    private static final String UUID_TEMPHUMID = "aa60";
    private static final String UUID_UNKNOWN = "0000";

    // 支持的设备类型的缺省名称
    private static final String NAME_SIMPLE128GATTPROFILE = "简单GATT例程";
    private static final String NAME_HEIGHTSCALE = "高度计";
    private static final String NAME_THERMOMETER = "体温计";
    private static final String NAME_ECGMONITOR = "心电监护仪";
    private static final String NAME_SIGGENERATOR = "信号发生器";
    private static final String NAME_TEMPHUMID = "温湿度计";
    private static final String NAME_UNKNOWN = "未知设备";

    // 支持的设备类型的Fragment类名
    private static final String FRAGNAME_SIMPLE128GATTPROFILE = "";
    private static final String FRAGNAME_HEIGHTSCALE = "";
    private static final String FRAGNAME_THERMOMETER = "com.cmtech.android.btdevice.thermo.ThermoFragment";
    private static final String FRAGNAME_ECGMONITOR = "com.cmtech.android.btdevice.ecgmonitor.EcgMonitorFragment";
    private static final String FRAGNAME_SIGGENERATOR = "";
    private static final String FRAGNAME_TEMPHUMID = "com.cmtech.android.btdevice.temphumid.TempHumidFragment";
    private static final String FRAGNAME_UNKNOWN = "com.cmtech.android.btdevice.unknown.UnknownDeviceFragment";

    private static final BLEDeviceType DEVTYPE_THERMOMETER =
            new BLEDeviceType(UUID_THERMOMETER, R.drawable.thermo_image, NAME_THERMOMETER, FRAGNAME_THERMOMETER);
    private static final BLEDeviceType DEVTYPE_ECGMONITOR =
            new BLEDeviceType(UUID_ECGMONITOR, R.drawable.ecgmonitor_image, NAME_ECGMONITOR, FRAGNAME_ECGMONITOR);
    private static final BLEDeviceType DEVTYPE_TEMPHUMID =
            new BLEDeviceType(UUID_TEMPHUMID, R.drawable.temphumid_image, NAME_TEMPHUMID, FRAGNAME_TEMPHUMID);
    private static final BLEDeviceType DEVTYPE_UNKNOWN =
            new BLEDeviceType(UUID_UNKNOWN, R.mipmap.ic_unknown_128px, NAME_UNKNOWN, FRAGNAME_UNKNOWN);

    private String uuid;        // 设备16位UUID字符串
    private Integer image;      // 缺省图标
    private String name;        // 缺省设备名
    private String fragName;    // 设备Fragment类名

    private BLEDeviceType(String uuid, Integer image, String name, String fragName) {
        this.uuid = uuid;
        this.image = image;
        this.name = name;
        this.fragName = fragName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getImage() {
        return image;
    }

    public void setImage(Integer image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFragName() {
        return fragName;
    }

    public void setFragName(String fragName) {
        this.fragName = fragName;
    }

    public static BLEDeviceType fromUuid(String uuid) {
        if(DEVTYPE_THERMOMETER.uuid.equalsIgnoreCase(uuid)) return DEVTYPE_THERMOMETER;
        if(DEVTYPE_ECGMONITOR.uuid.equalsIgnoreCase(uuid)) return DEVTYPE_ECGMONITOR;
        if(DEVTYPE_TEMPHUMID.uuid.equalsIgnoreCase(uuid)) return DEVTYPE_TEMPHUMID;
        return DEVTYPE_UNKNOWN;
    }
}
