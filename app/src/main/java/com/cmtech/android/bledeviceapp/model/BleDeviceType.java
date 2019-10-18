package com.cmtech.android.bledeviceapp.model;

import java.util.ArrayList;
import java.util.List;


/**
  *
  * ClassName:      BleDeviceType
  * Description:    Ble设备类型类
  * Author:         chenm
  * CreateDate:     2018-10-13 08:55
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-28 08:55
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class BleDeviceType {
    private static final List<BleDeviceType> SUPPORTED_DEVICE_TYPES = new ArrayList<>(); // 支持的设备类型数组

    private final String uuid; // 设备16位UUID字符串
    private final int defaultImageId; // 缺省图标ID
    private final String defaultNickname; // 缺省设备名
    private final String factoryClassName; // 设备工厂类名

    public BleDeviceType(String uuid, int defaultImageId, String defaultNickname, String factoryClassName) {
        this.uuid = uuid;
        this.defaultImageId = defaultImageId;
        this.defaultNickname = defaultNickname;
        this.factoryClassName = factoryClassName;
    }

    public static List<BleDeviceType> getSupportedDeviceTypes() {
        return SUPPORTED_DEVICE_TYPES;
    }

    public static void addSupportedType(BleDeviceType deviceType) {
        if(!SUPPORTED_DEVICE_TYPES.contains(deviceType)) {
            SUPPORTED_DEVICE_TYPES.add(deviceType);
        }
    }

    // 通过UUID获取对应的设备类型
    public static BleDeviceType getFromUuid(String uuid) {
        for(BleDeviceType type : SUPPORTED_DEVICE_TYPES) {
            if(type.uuid.equalsIgnoreCase(uuid)) {
                return type;
            }
        }
        return null;
    }

    public int getDefaultImageId() {
        return defaultImageId;
    }
    public String getDefaultNickname() {
        return defaultNickname;
    }
    public String getFactoryClassName() { return factoryClassName; }
}
