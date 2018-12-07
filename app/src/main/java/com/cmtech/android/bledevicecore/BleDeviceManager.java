package com.cmtech.android.bledevicecore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * BleDeviceManager: 整个设备的管理者，单例
 * Created by bme on 2018/12/08.
 */

public class BleDeviceManager {
    private static BleDeviceManager instance;

    private List<BleDevice> deviceList = new ArrayList<>();

    private BleDeviceManager() {

    }

    /**
     * 单例
     *
     * @return 返回BleDeviceManager
     */
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

    // 添加多个设备
    public void addDevice(List<BleDeviceBasicInfo> basicInfoList) {
        for(BleDeviceBasicInfo basicInfo : basicInfoList) {
            addDevice(basicInfo);
        }
    }

    // 添加单个设备
    public BleDevice addDevice(BleDeviceBasicInfo basicInfo) {
        BleDevice device = getDevice(basicInfo);
        if(device != null) return null;

        // 创建设备
        device = createBleDeviceUsingBasicInfo(basicInfo);
        // 将设备添加到设备列表
        deviceList.add(device);
        Collections.sort(deviceList, new Comparator<BleDevice>() {
            @Override
            public int compare(BleDevice o1, BleDevice o2) {
                return o1.getMacAddress().compareTo(o2.getMacAddress());
            }
        });
        return device;
    }

    // 获取设备清单
    public List<BleDevice> getDeviceList() {
        return deviceList;
    }

    // 根据设备基本信息创建一个新的设备，并添加到设备列表中
    private BleDevice createBleDeviceUsingBasicInfo(BleDeviceBasicInfo basicInfo) {
        // 获取相应的抽象工厂
        AbstractBleDeviceFactory factory = AbstractBleDeviceFactory.getBLEDeviceFactory(basicInfo);
        return (factory == null) ? null : factory.createBleDevice();
    }

    // 获取设备
    private BleDevice getDevice(BleDeviceBasicInfo basicInfo) {
        BleDevice findDevice = null;
        for(BleDevice device : deviceList) {
            if(device.getMacAddress().equalsIgnoreCase(basicInfo.getMacAddress())) {
                findDevice = device;
                break;
            }
        }
        return findDevice;
    }


}
