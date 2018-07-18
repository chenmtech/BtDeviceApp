package com.cmtech.android.btdeviceapp.interfa;

import android.bluetooth.BluetoothGattService;
import android.os.Message;

import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.BleDeviceConnectState;

import java.util.List;

public interface IBleDevice {
    public String getMacAddress();
    public void setMacAddress(String macAddress);
    public String getNickName();
    public void setNickName(String nickName);
    public String getUuidString();
    public void setUuidString(String uuidString);
    public boolean isAutoConnected();
    public void setAutoConnected(boolean autoConnected);
    public String getImagePath();
    public void setImagePath(String imagePath);
    public BleDeviceBasicInfo getBasicInfo();

    public void initialize();
    public BleDeviceConnectState getDeviceConnectState();
    public void setDeviceConnectState(BleDeviceConnectState state);
    public void connect();
    public void disconnect();
    public void close();

    public void registerConnectStateObserver(IBleDeviceConnectStateObserver observer);
    public void removeConnectStateObserver(IBleDeviceConnectStateObserver observer);
    public void notifyConnectStateObservers();

    public void executeAfterConnectSuccess();
    public void executeAfterConnectFailure();
    public void executeAfterDisconnect();
    public void processGattCallbackMessage(Message msg);
}
