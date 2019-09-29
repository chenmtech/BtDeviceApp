package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.text.TextUtils;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * BleDeviceManager: 设备管理器
 * Created by bme on 2018/12/08.
 */

public class BleDeviceManager {
    private static BleDeviceManager instance;
    private final List<BleDevice> deviceList = new ArrayList<>();

    private BleDeviceManager() {
    }

    public static BleDeviceManager getInstance() {
        if (instance == null) {
            synchronized (BleDeviceManager.class) {
                if (instance == null) {
                    instance = new BleDeviceManager();
                }
            }
        }
        return instance;
    }

    // 用注册信息创建一个设备，如果设备不存在
    public BleDevice createDeviceIfNotExist(Context context, BleDeviceRegisterInfo registerInfo) {
        BleDevice device = findDevice(registerInfo);
        if(device != null) return null;

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

    // 用注册信息寻找设备
    public BleDevice findDevice(BleDeviceRegisterInfo registerInfo) {
        return (registerInfo == null) ? null : findDevice(registerInfo.getMacAddress());
    }

    // 用mac地址寻找设备
    public BleDevice findDevice(String macAddress) {
        if(TextUtils.isEmpty(macAddress)) return null;

        for(BleDevice device : deviceList) {
            if(macAddress.equalsIgnoreCase(device.getMacAddress())) {
                return device;
            }
        }
        return null;
    }

    // 用设备注册信息创建设备
    private static BleDevice createDevice(Context context, BleDeviceRegisterInfo registerInfo) {
        // 获取相应的抽象工厂
        BleDeviceFactory factory = BleDeviceFactory.getBLEDeviceFactory(registerInfo);
        return (factory == null) ? null : factory.createDevice(context);
    }

    // 删除一个设备
    public void deleteDevice(BleDevice device) {
        deviceList.remove(device);
    }

    // 获取设备清单
    public List<BleDevice> getDeviceList() {
        return deviceList;
    }

    // 获取所有设备的Mac列表
    public List<String> getDeviceMacList() {
        List<String> deviceMacList = new ArrayList<>();
        for(BleDevice device : deviceList) {
            deviceMacList.add(device.getMacAddress());
        }
        return deviceMacList;
    }

    // 是否有设备打开了
    public boolean hasDeviceOpened() {
        for(BleDevice device : deviceList) {
            if(!device.isClosed()) {
                return true;
            }
        }
        return false;
    }
}
