package com.cmtech.android.btdeviceapp.model;

import android.util.Log;

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
        fragment = BLEDeviceAbstractFactory.getBLEDeviceFactory(device.getPersistantInfo()).createFragment();
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

    public void switchDevice() {
        DeviceState state = device.getDeviceState();
        Log.d("BLEDEVICECONTROLLER", "now the state is " + state);
        switch (state) {
            case CONNECT_SUCCESS:
                disconnectDevice();
                break;

            case CONNECT_PROCESS:
            case CONNECT_DISCONNECTING:
                break;

            default:
                connectDevice();
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
