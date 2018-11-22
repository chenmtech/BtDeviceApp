package com.cmtech.android.bledevicecore.model;

public class DeviceFragmentPair {
    // 设备
    private final BleDevice device;

    // Fragment
    private final BleDeviceFragment fragment;


    public DeviceFragmentPair(BleDevice device) {
        if(device == null) {
            throw new IllegalArgumentException();
        }

        this.device = device;
        fragment = createFragment(device);
    }

    public BleDevice getDevice() {
        return device;
    }

    public BleDeviceFragment getFragment() {
        return fragment;
    }

    // 为设备创建相应的Fragment
    private BleDeviceFragment createFragment(BleDevice device) {
        return BleDeviceAbstractFactory.getBLEDeviceFactory(device).createFragment();
    }
}
