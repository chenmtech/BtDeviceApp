package com.cmtech.android.ble.core;

import android.content.Context;

import com.cmtech.android.ble.exception.BleException;

public interface IDevice extends IConnector.IConnectorCallback {
    int INVALID_BATTERY = -1; // invalid battery level

    Context getContext();
    DeviceInfo getInfo();
    void updateInfo(DeviceInfo info);
    boolean isLocal(); // is local connCallback
    String getAddress();
    String getName();
    void setName(String name);
    String getUuid();
    String getIcon();
    int getBattery();
    void setBattery(final int battery);
    DeviceState getState(); // get state
    String getNotifyInfo();
    void setNotifyInfo(String notifyInfo);
    void addCommonListener(OnCommonDeviceListener listener);
    void removeCommonListener(OnCommonDeviceListener listener);

    void open(); // open connCallback
    void close(); // close connCallback
    void connect(); // connect connCallback
    void disconnect(boolean forever); // disconnect connCallback. if forever=true, no reconnection occurred, otherwise reconnect it.
    void switchState(); // switch state
    void handleException(BleException ex); // handle exception

    // common device listener interface
    interface OnCommonDeviceListener {
        void onStateUpdated(final IDevice device); // state updated
        void onBatteryUpdated(final IDevice device); // battery level updated
        void onNotificationInfoUpdated(final IDevice device); // notification info updated
    }
}
