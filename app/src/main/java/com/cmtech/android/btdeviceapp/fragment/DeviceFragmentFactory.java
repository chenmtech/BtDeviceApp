package com.cmtech.android.btdeviceapp.fragment;

import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDeviceType;

import static java.lang.Class.forName;

/**
 * Created by bme on 2018/2/28.
 */

public class DeviceFragmentFactory {

    private DeviceFragmentFactory() {

    }

    public static DeviceFragment build(BLEDeviceModel device) {
        MyBluetoothDeviceType deviceType = MyBluetoothDeviceType.fromUuid(device.getUuidString());
        try {
            DeviceFragment fragment = (DeviceFragment) Class.forName(deviceType.getFragName()).newInstance();
            return fragment;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }
}
