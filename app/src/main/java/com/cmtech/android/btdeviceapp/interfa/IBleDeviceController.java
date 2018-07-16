package com.cmtech.android.btdeviceapp.interfa;

import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;

public interface IBleDeviceController {
    public void connectDevice();
    public void disconnectDevice();
    public void closeDevice();
    public void switchDeviceConnectState();
    public IBleDevice getDevice();
    public BleDeviceFragment getFragment();

}
