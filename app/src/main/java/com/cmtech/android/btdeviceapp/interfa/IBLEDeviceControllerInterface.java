package com.cmtech.android.btdeviceapp.interfa;

import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;

public interface IBLEDeviceControllerInterface {
    public void connectDevice();
    public void disconnectDevice();
    public void closeDevice();
    public void switchDeviceConnectState();
    public IBLEDeviceInterface getDevice();
    public BLEDeviceFragment getFragment();

}
