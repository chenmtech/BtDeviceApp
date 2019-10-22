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
    private static final List<BleDevice> DEVICE_LIST = new ArrayList<>(); // 所有注册的设备列表

    // 如果设备不存在，用注册信息创建一个设备
    public static BleDevice createDeviceIfNotExist(Context context, BleDeviceRegisterInfo registerInfo) {
        BleDevice device = findDevice(registerInfo);
        if(device != null) {
            ViseLog.e("The device has existed.");
            return null;
        }
        device = createDevice(context, registerInfo); // 创建设备
        if(device == null) return null;

        DEVICE_LIST.add(device); // 将设备添加到设备列表
        // 按地址排序
        Collections.sort(DEVICE_LIST, new Comparator<BleDevice>() {
            @Override
            public int compare(BleDevice o1, BleDevice o2) {
                return o1.getMacAddress().compareTo(o2.getMacAddress());
            }
        });
        return device;
    }

    // 用注册信息寻找设备
    public static BleDevice findDevice(BleDeviceRegisterInfo registerInfo) {
        return (registerInfo == null) ? null : findDevice(registerInfo.getMacAddress());
    }

    // 用设备mac地址寻找设备
    public static BleDevice findDevice(String macAddress) {
        if(TextUtils.isEmpty(macAddress)) return null;
        for(BleDevice device : DEVICE_LIST) {
            if(macAddress.equalsIgnoreCase(device.getMacAddress())) {
                return device;
            }
        }
        return null;
    }

    private static BleDevice createDevice(Context context, BleDeviceRegisterInfo registerInfo) {
        BleFactory factory = BleFactory.getFactory(registerInfo); // 获取相应的工厂
        return (factory == null) ? null : factory.createDevice(context);
    }

    // 删除一个设备
    public static void deleteDevice(BleDevice device) {
        DEVICE_LIST.remove(device);
    }

    // 获取设备清单
    public static List<BleDevice> getDeviceList() {
        return DEVICE_LIST;
    }

    // 获取所有设备的Mac列表
    public static List<String> getDeviceMacList() {
        List<String> deviceMacList = new ArrayList<>();
        for(BleDevice device : DEVICE_LIST) {
            deviceMacList.add(device.getMacAddress());
        }
        return deviceMacList;
    }

    // 是否有打开的设备
    public static boolean hasOpenedDevice() {
        for(BleDevice device : DEVICE_LIST) {
            if(!device.isClosed()) {
                return true;
            }
        }
        return false;
    }
}
