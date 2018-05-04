package com.cmtech.android.btdeviceapp.fragment;

import com.cmtech.android.btdevice.ecgmonitor.EcgMonitorFragment;
import com.cmtech.android.btdevice.temphumid.TempHumidFragment;
import com.cmtech.android.btdevice.unknown.UnknownDeviceFragment;
import com.cmtech.android.btdevice.thermo.ThermoFragment;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDeviceType;
import com.cmtech.android.btdeviceapp.util.Uuid;

import java.util.UUID;

import static java.lang.Class.forName;

/**
 * Created by bme on 2018/2/28.
 */

public class DeviceFragmentFactory {

    private DeviceFragmentFactory() {

    }

    public static DeviceFragment build(MyBluetoothDevice device) {
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
