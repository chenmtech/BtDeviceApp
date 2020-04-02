package com.cmtech.android.ble.core;

public class BleDeviceInfo extends DeviceInfo {

    public BleDeviceInfo(String address, String uuid) {
        super(address, uuid, true);
    }

    private BleDeviceInfo(String address, String uuid, String name, String icon,
                          boolean autoConnect) {
        super(address, uuid, true, name, icon, autoConnect);
    }
}
