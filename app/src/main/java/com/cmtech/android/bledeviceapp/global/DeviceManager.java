package com.cmtech.android.bledeviceapp.global;

import static com.cmtech.android.ble.core.DeviceConnectState.CLOSED;

import android.content.Context;
import android.text.TextUtils;

import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.ble.core.OnDeviceListener;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
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
    private final List<IDevice> DEVICE_LIST = new ArrayList<>(); // 所有已注册设备列表

    DeviceManager() {
    }

    // create a new device
    public IDevice createNewDevice(Context context, DeviceCommonInfo info) {
        IDevice device = findDevice(info);
        if(device != null) {
            ViseLog.e("The device has existed.");
            return null;
        }
        device = createDevice(context, info); // 创建设备
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

    // find a device using info in DEVICE_LIST
    public IDevice findDevice(DeviceCommonInfo info) {
        return (info == null || TextUtils.isEmpty(info.getAddress())) ? null : findDevice(info.getAddress());
    }

    // find a device using address in DEVICE_LIST
    public IDevice findDevice(String address) {
        for(IDevice device : DEVICE_LIST) {
            if(address.equalsIgnoreCase(device.getAddress())) {
                return device;
            }
        }
        return null;
    }

    private IDevice createDevice(Context context, DeviceCommonInfo info) {
        DeviceFactory factory = DeviceFactory.getFactory(info); // 获取相应的工厂
        return (factory == null) ? null : factory.createDevice(context);
    }

    // 删除一个设备
    public void deleteDevice(IDevice device) {
        DEVICE_LIST.remove(device);
    }

    public List<IDevice> getBleDeviceList() {
        /*List<IDevice> devices = new ArrayList<>();
        for(IDevice device : DEVICE_LIST) {
            if(device.isLocal()) {
                devices.add(device);
            }
        }
        return devices;*/
        return DEVICE_LIST;
    }

    // 获取所有设备的Mac列表
    public List<String> getAddressList() {
        List<String> addresses = new ArrayList<>();
        for(IDevice device : DEVICE_LIST) {
            addresses.add(device.getAddress());
        }
        return addresses;
    }

    public List<IDevice> getOpenedDevice() {
        List<IDevice> devices = new ArrayList<>();

        for(IDevice device : DEVICE_LIST) {
            if(device.getConnectState() != CLOSED) {
                devices.add(device);
            }
        }
        return devices;
    }

    public void addCommonListenerForAllDevices(OnDeviceListener listener) {
        for(IDevice device : DEVICE_LIST) {
            device.addCommonListener(listener);
        }
    }

    public void removeCommonListenerForAllDevices(OnDeviceListener listener) {
        for(IDevice device : DEVICE_LIST) {
            device.removeCommonListener(listener);
        }
    }

    public void clear() {
        for(IDevice device : DEVICE_LIST) {
            if(device.getConnectState() != CLOSED)
                device.close();
        }
        DEVICE_LIST.clear();
    }

    // 是否有打开的设备
    public boolean hasDeviceOpen() {
        for(IDevice device : DEVICE_LIST) {
            if(device.getConnectState() != CLOSED) {
                return true;
            }
        }
        return false;
    }
}
