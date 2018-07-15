package com.cmtech.android.btdeviceapp.model;

import android.util.Log;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;

public class BleDeviceController implements IBleDeviceControllerInterface {
    // 主控制器
    private final MainController mainController;

    // 设备接口
    private final IBleDeviceInterface device;

    // 设备Fragment
    private final BleDeviceFragment fragment;


    public BleDeviceController(IBleDeviceInterface device, MainController mainController) {
        if(device == null || mainController == null) {
            throw new IllegalStateException();
        }

        this.mainController = mainController;
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
        mainController.closeDevice(device);
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
