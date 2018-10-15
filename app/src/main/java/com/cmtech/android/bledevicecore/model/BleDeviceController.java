package com.cmtech.android.bledevicecore.model;

public abstract class BleDeviceController {
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

    // 打开设备
    public void openDevice() {
        device.open();
    }

    /*public void connectDevice() {
        device.connect();
    }*/

    /*public void disconnectDevice() {
        device.disconnect();
    }*/

    // 关闭设备
    public void closeDevice() {
        device.close();
    }

    // 切换设备状态
    public void switchState() {
        device.switchState();
    }

    public BleDevice getDevice() {
        return device;
    }

    public BleDeviceFragment getFragment() {
        return fragment;
    }

    // 为设备创建Fragment
    private BleDeviceFragment createFragment(BleDevice device) {
        return BleDeviceAbstractFactory.getBLEDeviceFactory(device).createFragment();
    }
}
