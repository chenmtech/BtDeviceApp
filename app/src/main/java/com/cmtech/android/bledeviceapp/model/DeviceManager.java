package com.cmtech.android.bledeviceapp.model;

import android.text.TextUtils;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.BleDeviceState;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * ClassName:      DeviceManager
 * Description:    设备管理器
 * Author:         chenm
 * CreateDate:     2018-12-08 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2018-12-08 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class DeviceManager {
    private static final List<AbstractDevice> DEVICE_LIST = new ArrayList<>(); // 所有已注册设备列表

    // 如果设备不存在，用注册信息创建一个设备
    public static AbstractDevice createDeviceIfNotExist(DeviceRegisterInfo registerInfo) {
        AbstractDevice device = findDevice(registerInfo);
        if(device != null) {
            ViseLog.e("The device has existed.");
            return null;
        }
        device = createDevice(registerInfo); // 创建设备
        if(device == null) return null;

        DEVICE_LIST.add(device); // 将设备添加到设备列表
        // 按地址排序
        Collections.sort(DEVICE_LIST, new Comparator<AbstractDevice>() {
            @Override
            public int compare(AbstractDevice o1, AbstractDevice o2) {
                return o1.getAddress().compareTo(o2.getAddress());
            }
        });
        return device;
    }

    // 用注册信息寻找设备
    public static AbstractDevice findDevice(DeviceRegisterInfo registerInfo) {
        return (registerInfo == null) ? null : findDevice(registerInfo.getMacAddress());
    }

    // 用设备mac地址寻找设备
    public static AbstractDevice findDevice(String macAddress) {
        if(TextUtils.isEmpty(macAddress)) return null;
        for(AbstractDevice device : DEVICE_LIST) {
            if(macAddress.equalsIgnoreCase(device.getAddress())) {
                return device;
            }
        }
        return null;
    }

    private static AbstractDevice createDevice(DeviceRegisterInfo registerInfo) {
        DeviceFactory factory = DeviceFactory.getFactory(registerInfo); // 获取相应的工厂
        return (factory == null) ? null : factory.createDevice();
    }

    // 删除一个设备
    public static void deleteDevice(AbstractDevice device) {
        DEVICE_LIST.remove(device);
    }

    // 获取设备清单
    public static List<AbstractDevice> getDeviceList() {
        return DEVICE_LIST;
    }

    // 获取所有设备的Mac列表
    public static List<String> getDeviceMacList() {
        List<String> deviceMacList = new ArrayList<>();
        for(AbstractDevice device : DEVICE_LIST) {
            deviceMacList.add(device.getAddress());
        }
        return deviceMacList;
    }

    // 是否有打开的设备
    public static boolean hasOpenedDevice() {
        for(AbstractDevice device : DEVICE_LIST) {
            if(device.getState() != BleDeviceState.CLOSED) {
                return true;
            }
        }
        return false;
    }
}
