package com.cmtech.android.btdeviceapp.interfa;

import android.bluetooth.BluetoothGattService;
import android.os.Message;

import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.btdeviceapp.model.BLEDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.DeviceConnectState;

import java.util.List;

public interface IBLEDeviceModelInterface {
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
    public BLEDeviceBasicInfo getBasicInfo();

    public DeviceConnectState getDeviceConnectState();
    public void setDeviceConnectState(DeviceConnectState state);
    public void connect();
    public void disconnect();
    public void close();

    public List<BluetoothGattService> getServices();

    public void registerConnectStateObserver(IBLEDeviceConnectStateObserver observer);
    public void removeConnectStateObserver(IBLEDeviceConnectStateObserver observer);
    public void notifyConnectStateObservers();

    public void executeAfterConnectSuccess();
    public void executeAfterDisconnect(boolean isActive);
    public void processGattMessage(Message msg);
}
