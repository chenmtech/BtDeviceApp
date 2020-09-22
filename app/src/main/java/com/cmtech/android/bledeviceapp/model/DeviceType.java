package com.cmtech.android.bledeviceapp.model;

import static com.cmtech.android.bledeviceapp.global.AppConstant.SUPPORT_DEVICE_TYPES;


/**
  *
  * ClassName:      DeviceType
  * Description:    设备类型
  * Author:         chenm
  * CreateDate:     2018-10-13 08:55
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-28 08:55
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class DeviceType {
    private final String uuid; // 设备16位UUID字符串
    private final int defaultIcon; // 缺省图标ID
    private final String defaultName; // 缺省设备名
    private final String factoryClassName; // 设备工厂类名

    public DeviceType(String uuid, int defaultIcon, String defaultName, String factoryClassName) {
        this.uuid = uuid;
        this.defaultIcon = defaultIcon;
        this.defaultName = defaultName;
        this.factoryClassName = factoryClassName;
    }

    // 通过UUID获取对应的设备类型
    public static DeviceType getFromUuid(String uuid) {
        for(DeviceType type : SUPPORT_DEVICE_TYPES) {
            if(type.uuid.equalsIgnoreCase(uuid)) {
                return type;
            }
        }
        return null;
    }

    public String getUuid() {
        return uuid;
    }
    public int getDefaultIcon() {
        return defaultIcon;
    }
    public String getDefaultName() {
        return defaultName;
    }
    public String getFactoryClassName() { return factoryClassName; }
}
