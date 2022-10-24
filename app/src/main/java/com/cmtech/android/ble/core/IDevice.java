package com.cmtech.android.ble.core;

import android.content.Context;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;

/**
 * 所有低功耗蓝牙设备的共同接口
 * 它继承了连接器回调接口，也就是这个接口必须实现所有连接器回调操作
 */
public interface IDevice extends IConnectorCallback {
    // 无效电池电量常数
    int INVALID_BATTERY_LEVEL = -1;

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
    void addCommonListener(OnDeviceListener listener);
    void removeCommonListener(OnDeviceListener listener);
    BasicRecord getRecordingRecord();

    void open(); // open device
    void close(); // close device
    void connect(); // connect device
    void disconnect(boolean forever); // disconnect device. if forever=true, no reconnection occurred, otherwise reconnect it.
    void switchState(); // switch device state
    void handleException(BleException ex); // handle exception
}
