package com.cmtech.android.ble.core;

import android.content.Context;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;

public interface IDevice extends IConnector.IConnectorCallback {
    int INVALID_BATTERY_LEVEL = -1; // invalid battery level

    Context getContext();
    DeviceCommonInfo getCommonInfo();
    void updateCommonInfo(DeviceCommonInfo info);
    boolean isLocal(); // is local
    String getAddress();
    String getName();
    void setName(String name);
    String getUuid();
    String getIcon();
    int getBatteryLevel();
    void setBatteryLevel(final int batteryLevel);
    DeviceConnectState getConnectState(); // get connect state
    String getNotificationInfo();
    void setNotificationInfo(String notifyInfo);
    void addCommonListener(OnCommonDeviceListener listener);
    void removeCommonListener(OnCommonDeviceListener listener);
    BasicRecord getRecordingRecord();

    void open(); // open device
    void close(); // close device
    void connect(); // connect device
    void disconnect(boolean forever); // disconnect device. if forever=true, no reconnection occurred, otherwise reconnect it.
    void switchState(); // switch device state
    void handleException(BleException ex); // handle exception

    // common device listener interface
    interface OnCommonDeviceListener {
        void onConnectStateUpdated(final IDevice device); // connect state updated
        void onBatteryLevelUpdated(final IDevice device); // battery level updated
        void onNotificationInfoUpdated(final IDevice device); // notification info updated
    }
}
