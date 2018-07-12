package com.cmtech.android.btdeviceapp.interfa;

import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;

public interface IBleDeviceControllerInterface {
    public void connectDevice();
    public void disconnectDevice();
    public void closeDevice();
    public void switchDeviceConnectState();
    public IBleDeviceInterface getDevice();
    public BleDeviceFragment getFragment();

}
