package com.cmtech.android.bledevicecore.model;

/**
 * BleDeviceType：Ble设备类型
 * Created by bme on 2018/10/13.
 */

public class BleDeviceType {
    private String uuid;                  // 设备16位UUID字符串
    private int defaultImage;           // 缺省图标
    private String defaultNickname;     // 缺省设备名
    private String factoryClassName;    // 设备工厂类名

    public BleDeviceType(String uuid, int defaultImage, String defaultNickname, String factoryClassName) {
        this.uuid = uuid;
        this.defaultImage = defaultImage;
        this.defaultNickname = defaultNickname;
        this.factoryClassName = factoryClassName;
    }

    public String getUuid() {
        return uuid;
    }

    public int getDefaultImage() {
        return defaultImage;
    }

    public String getDefaultNickname() {
        return defaultNickname;
    }

    // 创建设备类型对应的抽象工厂
    public BleDeviceAbstractFactory createDeviceFactory() {
        if(factoryClassName == null || factoryClassName.equals("")) return null;

        BleDeviceAbstractFactory factory;
        try {
            factory = (BleDeviceAbstractFactory) Class.forName(factoryClassName).newInstance();
        } catch (Exception e) {
            factory = null;
        }
        return factory;
    }
}
