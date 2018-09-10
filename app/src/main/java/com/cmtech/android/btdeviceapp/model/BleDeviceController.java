package com.cmtech.android.btdeviceapp.model;

import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;

public class BleDeviceController {
    // 设备
    private final BleDevice device;

    // Fragment
    private final BleDeviceFragment fragment;


    public BleDeviceController(BleDevice device) {
        if(device == null) {
            throw new IllegalStateException();
        }

        this.device = device;
        // 为设备创建fragment，但是fragment还没有Attach到Activity
        fragment = BleDeviceAbstractFactory.getBLEDeviceFactory(device.getBasicInfo()).createFragment();
    }

    public void openDevice() {
        device.open();
    }

    public void scanDevice() {
        device.scan();
    }

    public void disconnectDevice() {
        device.disconnect();
    }

    public void closeDevice() {
        device.close();
    }

    public void switchDeviceConnectState() {
        device.switchState();
    }

    public BleDevice getDevice() {
        return device;
    }

    public BleDeviceFragment getFragment() {
        return fragment;
    }
}
