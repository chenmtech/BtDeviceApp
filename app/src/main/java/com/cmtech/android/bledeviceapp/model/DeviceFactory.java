package com.cmtech.android.bledeviceapp.model;

import android.content.Context;

import com.cmtech.android.ble.core.DeviceCommonInfo;
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
    protected final DeviceCommonInfo info; // device info

    protected DeviceFactory(DeviceCommonInfo info) {
        this.info = info;
    }

    // create device factory using device info
    public static DeviceFactory getFactory(DeviceCommonInfo info) {
        if(info == null) return null;

        DeviceType type = DeviceType.getFromUuid(info.getUuid());
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
            Constructor constructor = Class.forName(factoryClassName).getDeclaredConstructor(DeviceCommonInfo.class);
            constructor.setAccessible(true);
            factory = (DeviceFactory) constructor.newInstance(info);
        } catch (Exception e) {
            ViseLog.e("The device factory can't be found.");
            factory = null;
        }
        return factory;
    }

    public abstract IDevice createDevice(Context context); // create Device
    public abstract DeviceFragment createFragment(); // create Fragment
}
