package com.cmtech.android.ble.core;

import static com.cmtech.android.ble.core.DeviceConnectState.CONNECT;
import static com.cmtech.android.ble.core.DeviceConnectState.DISCONNECT;
import static com.cmtech.android.ble.core.DeviceConnectState.FAILURE;

import android.content.Context;
import android.widget.Toast;

import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.OtherException;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDevice implements IDevice{
    // 上下文
    private final Context context; // context
    private final DeviceCommonInfo commonInfo; // common information
    private int batteryLevel; // battery level
    private String notificationInfo;
    private final List<OnDeviceListener> commonListeners; // common device listeners
    protected final IConnector connector; // connector

    // 正在记录中的记录
    protected BasicRecord recordingRecord = null;

    public AbstractDevice(Context context, DeviceCommonInfo commonInfo) {
        if(context == null) {
            throw new NullPointerException("The context is null.");
        }
        if(commonInfo == null) {
            throw new NullPointerException("The info is null.");
        }
        this.context = context;
        this.commonInfo = commonInfo;
        if(commonInfo.isLocal()) {
            connector = new BleConnector(this.getAddress(), this);
        } else {
            connector = new WebConnector(this.getAddress(), this);
        }
        commonListeners = new ArrayList<>();
        batteryLevel = INVALID_BATTERY_LEVEL;
        notificationInfo = "";
    }

    @Override
    public Context getContext() {
        return context;
    }
    @Override
    public DeviceCommonInfo getCommonInfo() {
        return commonInfo;
    }
    @Override
    public void updateCommonInfo(DeviceCommonInfo info) {
        this.commonInfo.update(info);
    }
    @Override
    public boolean isLocal() {
        return commonInfo.isLocal();
    }
    @Override
    public final String getAddress() {
        return commonInfo.getAddress();
    }
    @Override
    public String getUuid() {
        return commonInfo.getUuid();
    }
    @Override
    public String getName() {
        return commonInfo.getName();
    }
    @Override
    public void setName(String name) {
        commonInfo.setName(name);
    }
    @Override
    public String getIcon() {
        return commonInfo.getIcon();
    }
    @Override
    public int getBatteryLevel() {
        return batteryLevel;
    }
    @Override
    public void setBatteryLevel(final int batteryLevel) {
        if(this.batteryLevel != batteryLevel) {
            this.batteryLevel = batteryLevel;
            for (final OnDeviceListener listener : commonListeners) {
                if (listener != null) {
                    listener.onBatteryLevelUpdated(this);
                }
            }
        }
    }
    @Override
    public String getNotificationInfo() {
        return notificationInfo;
    }

    @Override
    public void setNotificationInfo(String notificationInfo) {
        this.notificationInfo = notificationInfo;
        for (final OnDeviceListener listener : commonListeners) {
            if (listener != null) {
                listener.onNotificationInfoUpdated(this);
            }
        }
    }

    @Override
    public final void addCommonListener(OnDeviceListener listener) {
        if(!commonListeners.contains(listener)) {
            commonListeners.add(listener);
        }
    }

    @Override
    public final void removeCommonListener(OnDeviceListener listener) {
        commonListeners.remove(listener);
    }

    @Override
    public DeviceConnectState getConnectState() {
        return connector.getState();
    }

    @Override
    public BasicRecord getRecordingRecord() {
        return recordingRecord;
    }

    @Override
    public void open() {
        if (context == null) {
            throw new NullPointerException("The context is null.");
        }

        if (connector.open(context) && commonInfo.isAutoConnect())
            connect();
    }

    @Override
    public void close() {
        connector.close();
    }

    @Override
    public void connect() {
        connector.connect();
    }

    @Override
    public void disconnect(boolean forever) {
        connector.disconnect(forever);
    }

    // 切换状态
    @Override
    public void switchState() {
        ViseLog.e("Device.switchState()");
        if (connector.getState() == FAILURE || connector.getState() == DISCONNECT) {
            connect();
        } else if (connector.getState() == CONNECT) {
            disconnect(true);
        } else { // 无效操作
            handleException(new OtherException(context.getString(R.string.invalid_operation)));
        }
    }

    @Override
    public void handleException(BleException ex) {
        if(context != null)
            Toast.makeText(context, ex.getDescription(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectStateUpdated() {
        for(OnDeviceListener listener : commonListeners) {
            if(listener != null) {
                listener.onConnectStateUpdated(this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractDevice)) return false;
        AbstractDevice that = (AbstractDevice) o;
        return commonInfo.equals(that.commonInfo);
    }

    @Override
    public int hashCode() {
        return (commonInfo != null) ? commonInfo.hashCode() : 0;
    }

}
