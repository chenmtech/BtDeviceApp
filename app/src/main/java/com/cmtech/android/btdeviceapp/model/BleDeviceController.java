package com.cmtech.android.btdeviceapp.model;

import android.util.Log;

import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceController;
import com.cmtech.android.btdeviceapp.interfa.IBleDevice;

public class BleDeviceController implements IBleDeviceController {
    // 设备
    private final IBleDevice device;

    // Fragment
    private final BleDeviceFragment fragment;


    public BleDeviceController(IBleDevice device) {
        if(device == null) {
            throw new IllegalStateException();
        }

        this.device = device;
        // 为设备创建fragment，但是fragment还没有Attach到Activity
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
    public IBleDevice getDevice() {
        return device;
    }

    @Override
    public BleDeviceFragment getFragment() {
        return fragment;
    }
}
