package com.cmtech.android.ble.core;

import android.content.Context;

import com.cmtech.android.ble.exception.BleException;

public interface IDevice extends IConnector.IConnectorCallback {
    int INVALID_BATTERY = -1; // invalid battery level

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
    void addListener(OnDeviceListener listener);
    void removeListener(OnDeviceListener listener);

    void open(Context context); // open connCallback
    void close(); // close connCallback
    void connect(); // connect connCallback
    void disconnect(boolean forever); // disconnect connCallback. if forever=true, no reconnection occurred, otherwise reconnect it.
    void switchState(); // switch state
    void handleException(BleException ex); // handle exception

    // connCallback listener interface
    interface OnDeviceListener {
        void onStateUpdated(final IDevice device); // state updated
        void onExceptionNotified(final IDevice device, BleException ex); // exception notified
        void onBatteryUpdated(final IDevice device); // battery level updated
        void onNotificationInfoUpdated(final IDevice device);
    }
}
