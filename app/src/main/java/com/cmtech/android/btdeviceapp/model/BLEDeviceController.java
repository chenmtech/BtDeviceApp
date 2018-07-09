package com.cmtech.android.btdeviceapp.model;

import android.util.Log;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.BLEDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceInterface;

public class BLEDeviceController implements IBLEDeviceControllerInterface{
    private final MainActivity activity;

    // Model
    private final IBLEDeviceInterface device;

    // View
    private final BLEDeviceFragment fragment;


    public BLEDeviceController(IBLEDeviceInterface device, MainActivity activity) {
        if(device == null || activity == null) {
            throw new IllegalStateException();
        }

        this.activity = activity;
        this.device = device;
        fragment = BLEDeviceAbstractFactory.getBLEDeviceFactory(device.getBasicInfo()).createFragment();
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
        DeviceConnectState state = device.getDeviceConnectState();
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
    public IBLEDeviceInterface getDevice() {
        return device;
    }

    @Override
    public BLEDeviceFragment getFragment() {
        return fragment;
    }
}
