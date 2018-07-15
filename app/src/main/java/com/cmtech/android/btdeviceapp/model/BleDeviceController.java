package com.cmtech.android.btdeviceapp.model;

import android.util.Log;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;

public class BleDeviceController implements IBleDeviceControllerInterface {
    private final MainActivity activity;

    // Model
    private final IBleDeviceInterface device;

    // View
    private final BleDeviceFragment fragment;


    public BleDeviceController(IBleDeviceInterface device, MainActivity activity) {
        if(device == null || activity == null) {
            throw new IllegalStateException();
        }

        this.activity = activity;
        this.device = device;
        fragment = BleDeviceAbstractFactory.getBLEDeviceFactory(device.getBasicInfo()).createFragment();
    }

    @Override
    public void connectDevice() {
        device.connect();
    }

    @Override
    public void disconnectDevice() {
        device.disconnect();
    }

    @Override
    public void closeDevice() {
        device.close();
    }

    @Override
    public void switchDeviceConnectState() {
        BleDeviceConnectState state = device.getDeviceConnectState();
        Log.d("BLEDEVICECONTROLLER", "now the state is " + state);
        switch (state) {
            case CONNECT_SUCCESS:
                disconnectDevice();
                break;

            case CONNECT_CONNECTING:
            case CONNECT_DISCONNECTING:
                break;

            default:
                connectDevice();
                break;
        }
    }

    @Override
    public IBleDeviceInterface getDevice() {
        return device;
    }

    @Override
    public BleDeviceFragment getFragment() {
        return fragment;
    }
}
