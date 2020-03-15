package com.cmtech.android.bledeviceapp.model;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.ble.core.WebDeviceRegisterInfo;
import com.cmtech.android.bledevice.ecg.webecg.EcgHttpReceiver;
import com.cmtech.android.bledevice.ecg.webecg.WebEcgDevice;
import com.cmtech.android.bledeviceapp.util.UserUtil;
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
    private static final List<IDevice> DEVICE_LIST = new ArrayList<IDevice>(); // 所有已注册设备列表

    // 如果设备不存在，用注册信息创建一个设备
    public static IDevice createDeviceIfNotExist(DeviceRegisterInfo registerInfo) {
        IDevice device = findDevice(registerInfo);
        if(device != null) {
            ViseLog.e("The device has existed.");
            return null;
        }
        device = createDevice(registerInfo); // 创建设备
        if(device == null) return null;

        DEVICE_LIST.add(device); // 将设备添加到设备列表
        // 按地址排序
        Collections.sort(DEVICE_LIST, new Comparator<IDevice>() {
            @Override
            public int compare(IDevice o1, IDevice o2) {
                return o1.getAddress().compareTo(o2.getAddress());
            }
        });
        return device;
    }

    // 用注册信息寻找设备
    public static IDevice findDevice(DeviceRegisterInfo registerInfo) {
        return (registerInfo == null) ? null : findDevice(registerInfo.getMacAddress());
    }

    // 用设备mac地址寻找设备
    public static IDevice findDevice(String macAddress) {
        if(TextUtils.isEmpty(macAddress)) return null;
        for(IDevice device : DEVICE_LIST) {
            if(macAddress.equalsIgnoreCase(device.getAddress())) {
                return device;
            }
        }
        return null;
    }

    private static IDevice createDevice(DeviceRegisterInfo registerInfo) {
        DeviceFactory factory = DeviceFactory.getFactory(registerInfo); // 获取相应的工厂
        return (factory == null) ? null : factory.createDevice();
    }

    // 删除一个设备
    public static void deleteDevice(IDevice device) {
        DEVICE_LIST.remove(device);
    }

    // 获取设备清单
    public static List<IDevice> getDeviceList() {
        return DEVICE_LIST;
    }

    public static List<IDevice> getBleDeviceList() {
        List<IDevice> devices = new ArrayList<>();
        for(IDevice device : DEVICE_LIST) {
            if(device.isLocal()) {
                devices.add(device);
            }
        }
        return devices;
    }

    public static List<IDevice> getWebDeviceList() {
        List<IDevice> devices = new ArrayList<>();
        for(IDevice device : DEVICE_LIST) {
            if(!device.isLocal()) {
                devices.add(device);
            }
        }
        return devices;
    }

    // 获取所有设备的Mac列表
    public static List<String> getDeviceMacList() {
        List<String> deviceMacList = new ArrayList<>();
        for(IDevice device : DEVICE_LIST) {
            deviceMacList.add(device.getAddress());
        }
        return deviceMacList;
    }

    public static List<IDevice> getOpenedDevice() {
        List<IDevice> devices = new ArrayList<>();

        for(IDevice device : DEVICE_LIST) {
            if(device.getState() != BleDeviceState.CLOSED) {
                devices.add(device);
            }
        }
        return devices;
    }

    public static void removeDeviceListener(IDevice.OnDeviceListener listener) {
        for(IDevice device : DEVICE_LIST) {
            device.removeListener(listener);
        }
    }

    public static void addDeviceListener(IDevice.OnDeviceListener listener) {
        for(IDevice device : DEVICE_LIST) {
            device.addListener(listener);
        }
    }

    public static void clearDevices() {
        for(IDevice device : DEVICE_LIST) {
            device.close();
        }
    }

    // 是否有打开的设备
    public static boolean hasOpenedDevice() {
        for(IDevice device : DEVICE_LIST) {
            if(device.getState() != BleDeviceState.CLOSED) {
                return true;
            }
        }
        return false;
    }

    public static void updateWebDevices() {
        // 获取网络广播设备列表
        final List<IDevice> currentWebDevices = getWebDeviceList();

        for(IDevice device : currentWebDevices) {
            if(device.getState() == BleDeviceState.CLOSED) {
                DEVICE_LIST.remove(device);
            }
        }

        final boolean[] finish = new boolean[1];

        EcgHttpReceiver.retrieveDeviceInfo(AccountManager.getInstance().getAccount().getHuaweiId(), new EcgHttpReceiver.IEcgDeviceInfoCallback() {
            @Override
            public void onReceived(List<WebEcgDevice> deviceList) {
                if(deviceList == null || deviceList.isEmpty()) {
                    finish[0] = true;
                    return;
                }

                final int[] update = new int[]{deviceList.size()};
                for(WebEcgDevice device : deviceList) {
                    final WebDeviceRegisterInfo registerInfo = (WebDeviceRegisterInfo)device.getRegisterInfo();
                    UserUtil.getUserInfo(registerInfo.getBroadcastId(), new UserUtil.IGetUserInfoCallback() {
                        @Override
                        public void onReceived(String userId, final String name, String description, Bitmap image) {
                            if(!TextUtils.isEmpty(name))
                                registerInfo.setBroadcastName(name);
                            update[0]--;
                        }
                    });
                }

                while(update[0] > 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                finish[0] = true;
            }
        });

        while (!finish[0]) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
