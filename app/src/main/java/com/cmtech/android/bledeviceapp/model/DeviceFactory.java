package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.vise.log.ViseLog;

import java.lang.reflect.Constructor;

/**
 *
 * ClassName:      DeviceFactory
 * Description:    设备工厂抽象类
 * Author:         chenm
 * CreateDate:     2018-10-13 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2018-10-13 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */


public abstract class DeviceFactory {
    protected final DeviceRegisterInfo registerInfo; // 设备注册信息

    protected DeviceFactory(DeviceRegisterInfo registerInfo) {
        this.registerInfo = registerInfo;
    }

    // 获取注册信息对应的工厂
    public static DeviceFactory getFactory(DeviceRegisterInfo registerInfo) {
        if(registerInfo == null) return null;

        DeviceType type = DeviceType.getFromUuid(registerInfo.getUuidStr());
        if(type == null) {
            ViseLog.e("The device type is not supported.");
            return null;
        }

        String factoryClassName = type.getFactoryClassName();
        if(factoryClassName == null) {
            ViseLog.e("The factory class name is null.");
            return null;
        }

        DeviceFactory factory;
        try {
            Constructor constructor = Class.forName(factoryClassName).getDeclaredConstructor(DeviceRegisterInfo.class);
            constructor.setAccessible(true);
            factory = (DeviceFactory) constructor.newInstance(registerInfo);
        } catch (Exception e) {
            ViseLog.e("The device factory can't be found.");
            factory = null;
        }
        return factory;
    }

    public abstract IDevice createDevice(); // 创建Device
    public abstract DeviceFragment createFragment(); // 创建Fragment
}
