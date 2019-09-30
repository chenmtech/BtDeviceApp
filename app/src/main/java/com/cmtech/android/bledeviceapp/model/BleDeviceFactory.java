package com.cmtech.android.bledeviceapp.model;

import android.content.Context;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.BleDeviceType;
import com.cmtech.android.bledeviceapp.activity.BleFragment;
import com.vise.log.ViseLog;

import java.lang.reflect.Constructor;

/*
 * BleDeviceFactory：Ble设备抽象工厂
 * Created by bme on 2018/10/13.
 */

public abstract class BleDeviceFactory {
    protected BleDeviceRegisterInfo registerInfo; // 设备注册信息对象

    protected BleDeviceFactory(BleDeviceRegisterInfo registerInfo) {
        this.registerInfo = registerInfo;
    }

    // 获取注册信息对应的设备抽象工厂
    public static BleDeviceFactory getBLEDeviceFactory(BleDeviceRegisterInfo registerInfo) {
        if(registerInfo == null) return null;

        BleDeviceType type = BleDeviceType.getFromUuid(registerInfo.getUuidString());
        if(type == null) {
            ViseLog.e("BleDeviceType is not supported.");
            return null;
        }

        String factoryClassName = type.getFactoryClassName();
        if(factoryClassName == null) {
            return null;
        }

        BleDeviceFactory factory;
        try {
            Constructor constructor = Class.forName(factoryClassName).getDeclaredConstructor(BleDeviceRegisterInfo.class);
            constructor.setAccessible(true);
            factory = (BleDeviceFactory) constructor.newInstance(registerInfo);
        } catch (Exception e) {
            factory = null;
        }
        return factory;
    }

    public abstract BleDevice createDevice(Context context); // 创建BleDevice
    public abstract BleFragment createFragment(); // 创建Fragment
}
