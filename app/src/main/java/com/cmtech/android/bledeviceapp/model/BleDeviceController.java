package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.bledevicecore.model.BleDevice;

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
        fragment = createFragment(device);
    }

    public void openDevice() {
        device.open();
    }

    public void connectDevice() {
        device.connect();
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

    private BleDeviceFragment createFragment(BleDevice device) {
        return BleDeviceAbstractFactory.getBLEDeviceFactory(device).createFragment();
    }
}
