package com.cmtech.android.ble.core;

public class BleDeviceCommonInfo extends DeviceCommonInfo {

    public BleDeviceCommonInfo(String address, String uuid) {
        super(address, uuid, true);
    }

    private BleDeviceCommonInfo(String address, String uuid, String name, String icon,
                                boolean autoConnect) {
        super(address, uuid, true, name, icon, autoConnect);
    }
}
