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
        fragment = BleDeviceAbstractFactory.getBLEDeviceFactory(device.getBasicInfo()).createFragment();;
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

    public void switchState() {
        device.switchState();
    }

    public BleDevice getDevice() {
        return device;
    }

    public BleDeviceFragment getFragment() {
        return fragment;
    }
}
