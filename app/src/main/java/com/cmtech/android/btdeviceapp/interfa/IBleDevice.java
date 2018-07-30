package com.cmtech.android.btdeviceapp.interfa;


import android.os.Message;

import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.BleDeviceConnectState;



public interface IBleDevice {
    public String getMacAddress();
    public String getNickName();
    public String getUuidString();
    public boolean isAutoConnected();
    public String getImagePath();
    public BleDeviceBasicInfo getBasicInfo();


    public BleDeviceConnectState getDeviceConnectState();
    public void setDeviceConnectState(BleDeviceConnectState state);
    public void connect();
    public void disconnect();
    public void close();

    public void registerConnectStateObserver(IBleDeviceConnectStateObserver observer);
    public void removeConnectStateObserver(IBleDeviceConnectStateObserver observer);
    public void notifyConnectStateObservers();

    public void initialize();
    public void executeAfterConnectSuccess();
    public void executeAfterConnectFailure();
    public void executeAfterDisconnect();
    public void processGattCallbackMessage(Message msg);
}
