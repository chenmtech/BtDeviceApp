package com.cmtech.android.bledeviceapp.model;

import android.content.Context;

import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceBasicInfo;
import com.cmtech.android.ble.extend.BleDeviceType;
import com.cmtech.android.bledeviceapp.activity.BleDeviceFragment;

/*
 * AbstractBleDeviceFactory：Ble设备抽象工厂
 * Created by bme on 2018/10/13.
 */

public abstract class AbstractBleDeviceFactory {
    // 工厂要用的设备基本信息对象
    protected BleDeviceBasicInfo basicInfo;

    // 获取BleDevice对应的抽象工厂
    public static AbstractBleDeviceFactory getBLEDeviceFactory(BleDevice device) {
        return (device == null) ? null : getBLEDeviceFactory(device.getBasicInfo());
    }

    // 获取BleDeviceBasicInfo对应的抽象工厂
    public static AbstractBleDeviceFactory getBLEDeviceFactory(BleDeviceBasicInfo basicInfo) {
        AbstractBleDeviceFactory factory = null;
        if(basicInfo != null) {
            factory = getBLEDeviceFactory(basicInfo.getUuidString());
        }
        if(factory != null) {
            factory.basicInfo = basicInfo;
        }
        return factory;
    }

    // 创建BleDevice
    public abstract BleDevice createDevice(Context context);

    // 创建Fragment
    public abstract BleDeviceFragment createFragment();

    private static AbstractBleDeviceFactory getBLEDeviceFactory(String uuidString) {
        return getBLEDeviceFactory(BleDeviceType.getFromUuid(uuidString));
    }

    private static AbstractBleDeviceFactory getBLEDeviceFactory(BleDeviceType deviceType) {
        if(deviceType == null || deviceType.getFactoryClassName() == null) return null;

        String factoryClassName = deviceType.getFactoryClassName();

        AbstractBleDeviceFactory factory;

        try {
            factory = (AbstractBleDeviceFactory) Class.forName(factoryClassName).newInstance();
        } catch (Exception e) {
            factory = null;
        }

        return factory;
    }
}
