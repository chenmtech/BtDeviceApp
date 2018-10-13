package com.cmtech.android.bledevicecore.model;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorDeviceFactory;
import com.cmtech.android.bledevice.temphumid.TempHumidDeviceFactory;
import com.cmtech.android.bledevice.thermo.ThermoDeviceFactory;

import static com.cmtech.android.bledevicecore.model.BleDeviceType.DEVTYPE_ECGMONITOR;
import static com.cmtech.android.bledevicecore.model.BleDeviceType.DEVTYPE_TEMPHUMID;
import static com.cmtech.android.bledevicecore.model.BleDeviceType.DEVTYPE_THERMOMETER;

/*
 * BleDeviceAbstractFactory：Ble设备抽象工厂
 * Created by bme on 2018/10/13.
 */
public abstract class BleDeviceAbstractFactory {
    // 获取BleDevice的抽象工厂
    public static BleDeviceAbstractFactory getBLEDeviceFactory(BleDevice device) {
        return (device == null) ? null : getBLEDeviceFactory(device.getBasicInfo());
    }

    public static BleDeviceAbstractFactory getBLEDeviceFactory(BleDeviceBasicInfo basicInfo) {
        return (basicInfo == null) ? null : getBLEDeviceFactory(basicInfo.getUuidString());
    }

    public static BleDeviceAbstractFactory getBLEDeviceFactory(String uuidString) {
        return getBLEDeviceFactory(BleDeviceType.fromUuid(uuidString));
    }

    public static BleDeviceAbstractFactory getBLEDeviceFactory(BleDeviceType deviceType) {
        if(DEVTYPE_TEMPHUMID == deviceType)
            return new TempHumidDeviceFactory();
        else if(DEVTYPE_ECGMONITOR == deviceType)
            return new EcgMonitorDeviceFactory();
        else if(DEVTYPE_THERMOMETER == deviceType)
            return new ThermoDeviceFactory();
        else
            return null;
    }

    // 创建BleDevice
    public abstract BleDevice createBleDevice(BleDeviceBasicInfo basicInfo);
    // 创建控制器
    public abstract BleDeviceController createController(BleDevice device);
    // 创建Fragment
    public abstract BleDeviceFragment createFragment();
}
