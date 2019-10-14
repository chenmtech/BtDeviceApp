package com.cmtech.android.bledeviceapp.model;

import android.content.Context;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.bledeviceapp.activity.BleFragment;
import com.vise.log.ViseLog;

import java.lang.reflect.Constructor;

/**
 *
 * ClassName:      BleFactory
 * Description:    Ble工厂抽象类
 * Author:         chenm
 * CreateDate:     2018-10-13 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2018-10-13 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */


public abstract class BleFactory {
    protected BleDeviceRegisterInfo registerInfo; // 设备注册信息

    protected BleFactory(BleDeviceRegisterInfo registerInfo) {
        this.registerInfo = registerInfo;
    }

    // 获取注册信息对应的工厂
    public static BleFactory getFactory(BleDeviceRegisterInfo registerInfo) {
        if(registerInfo == null) return null;

        BleDeviceType type = BleDeviceType.getFromUuid(registerInfo.getUuidStr());
        if(type == null) {
            ViseLog.e("The device type is not supported.");
            return null;
        }

        String factoryClassName = type.getFactoryClassName();
        if(factoryClassName == null) {
            return null;
        }

        BleFactory factory;
        try {
            Constructor constructor = Class.forName(factoryClassName).getDeclaredConstructor(BleDeviceRegisterInfo.class);
            constructor.setAccessible(true);
            factory = (BleFactory) constructor.newInstance(registerInfo);
        } catch (Exception e) {
            ViseLog.e("The device factory can't be created.");
            factory = null;
        }
        return factory;
    }

    public abstract BleDevice createDevice(Context context); // 创建Device
    public abstract BleFragment createFragment(); // 创建Fragment
}
