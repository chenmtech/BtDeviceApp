package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.text.TextUtils;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * ClassName:      BleDeviceManager
 * Description:    设备管理器
 * Author:         chenm
 * CreateDate:     2018-12-08 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2018-12-08 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class BleDeviceManager {
    private static final List<BleDevice> deviceList = new ArrayList<>();

    private BleDeviceManager() {
    }

    // 如果设备不存在，用注册信息创建一个设备
    public static BleDevice createDeviceIfNotExist(Context context, BleDeviceRegisterInfo registerInfo) {
        BleDevice device = findDevice(registerInfo);
        if(device != null) {
            ViseLog.e("The device has existed.");
            return null;
        }

        // 创建设备
        device = createDevice(context, registerInfo);
        if(device == null) return null;

        // 将设备添加到设备列表
        deviceList.add(device);
        // 按地址排序
        Collections.sort(deviceList, new Comparator<BleDevice>() {
            @Override
            public int compare(BleDevice o1, BleDevice o2) {
                return o1.getMacAddress().compareTo(o2.getMacAddress());
            }
        });
        return device;
    }

    private static BleDevice createDevice(Context context, BleDeviceRegisterInfo registerInfo) {
        // 获取相应的工厂
        BleFactory factory = BleFactory.getFactory(registerInfo);
        return (factory == null) ? null : factory.createDevice(context);
    }

    // 用注册信息寻找设备
    public static BleDevice findDevice(BleDeviceRegisterInfo registerInfo) {
        return (registerInfo == null) ? null : findDevice(registerInfo.getMacAddress());
    }

    // 用mac地址寻找设备
    public static BleDevice findDevice(String macAddress) {
        if(TextUtils.isEmpty(macAddress)) return null;

        for(BleDevice device : deviceList) {
            if(macAddress.equalsIgnoreCase(device.getMacAddress())) {
                return device;
            }
        }
        return null;
    }

    // 删除一个设备
    public static void deleteDevice(BleDevice device) {
        deviceList.remove(device);
    }

    // 获取设备清单
    public static List<BleDevice> getDeviceList() {
        return deviceList;
    }

    // 获取所有设备的Mac列表
    public static List<String> getDeviceMacList() {
        List<String> deviceMacList = new ArrayList<>();
        for(BleDevice device : deviceList) {
            deviceMacList.add(device.getMacAddress());
        }
        return deviceMacList;
    }

    // 是否有设备打开了
    public static boolean existOpenedDevice() {
        for(BleDevice device : deviceList) {
            if(!device.isClosed()) {
                return true;
            }
        }
        return false;
    }
}
