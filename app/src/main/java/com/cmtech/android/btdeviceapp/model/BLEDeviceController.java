package com.cmtech.android.btdeviceapp.model;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.BLEDeviceAbstractFactory;

public class BLEDeviceController {
    private final MainActivity activity;

    private final BLEDeviceModel device;
    private final BLEDeviceFragment fragment;

    public BLEDeviceController(BLEDeviceModel device, MainActivity activity) {
        if(device == null || activity == null) {
            throw new IllegalStateException();
        }

        this.activity = activity;
        this.device = device;
        fragment = BLEDeviceAbstractFactory.getBLEDeviceFactory(device).createFragment();

        activity.addFragmentToManager(this);
        //connectDevice();
    }

    public void connectDevice() {
        device.connect();
    }

    public void disconnectDevice() {
        device.disconnect();
    }

    public void switchDevice() {
        DeviceState state = device.getDeviceState();
        switch (state) {
            case CONNECT_SUCCESS:
                disconnectDevice();
                break;
            case CONNECT_DISCONNECT:
            case CONNECT_WAITING:
                connectDevice();
                break;

            default:
                break;
        }
    }

    public BLEDeviceModel getDevice() {
        return device;
    }

    public BLEDeviceFragment getFragment() {
        return fragment;
    }
}
