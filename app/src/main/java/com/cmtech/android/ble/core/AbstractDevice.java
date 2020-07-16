package com.cmtech.android.ble.core;

import android.content.Context;

import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.OtherException;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.ble.core.DeviceState.CONNECT;
import static com.cmtech.android.ble.core.DeviceState.DISCONNECT;
import static com.cmtech.android.ble.core.DeviceState.FAILURE;

public abstract class AbstractDevice implements IDevice{
    private Context context; // context
    private final DeviceInfo info; // information
    private int battery; // battery level
    private String notifyInfo;
    private final List<OnCommonDeviceListener> listeners; // connCallback listeners
    protected final IConnector connector; // connector

    public AbstractDevice(Context context, DeviceInfo info) {
        if(info == null) {
            throw new NullPointerException("The info is null.");
        }
        this.context = context;
        this.info = info;
        if(info.isLocal()) {
            connector = new BleConnector(this.getAddress(), this);
        } else {
            connector = new WebConnector(this.getAddress(), this);
        }
        listeners = new ArrayList<>();
        battery = INVALID_BATTERY;
        notifyInfo = "";
    }

    @Override
    public Context getContext() {
        return context;
    }
    @Override
    public DeviceInfo getInfo() {
        return info;
    }
    @Override
    public void updateInfo(DeviceInfo info) {
        this.info.update(info);
    }
    @Override
    public boolean isLocal() {
        return info.isLocal();
    }
    @Override
    public final String getAddress() {
        return info.getAddress();
    }
    @Override
    public String getUuid() {
        return info.getUuid();
    }
    @Override
    public String getName() {
        return info.getName();
    }
    @Override
    public void setName(String name) {
        info.setName(name);
    }
    @Override
    public String getIcon() {
        return info.getIcon();
    }
    @Override
    public int getBattery() {
        return battery;
    }
    @Override
    public void setBattery(final int battery) {
        if(this.battery != battery) {
            this.battery = battery;
            for (final OnCommonDeviceListener listener : listeners) {
                if (listener != null) {
                    listener.onBatteryUpdated(this);
                }
            }
        }
    }
    @Override
    public String getNotifyInfo() {
        return notifyInfo;
    }

    @Override
    public void setNotifyInfo(String notifyInfo) {
        this.notifyInfo = notifyInfo;
        for (final OnCommonDeviceListener listener : listeners) {
            if (listener != null) {
                listener.onNotificationInfoUpdated(this);
            }
        }
    }

    @Override
    public final void addCommonListener(OnCommonDeviceListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public final void removeCommonListener(OnCommonDeviceListener listener) {
        listeners.remove(listener);
    }

    @Override
    public DeviceState getState() {
        return connector.getState();
    }

    @Override
    public void open() {
        if (context == null) {
            throw new NullPointerException("The context is null.");
        }

        if (connector.open(context) && info.isAutoConnect())
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
            if(context != null)
                handleException(new OtherException(context.getString(R.string.invalid_operation)));
        }
    }

    @Override
    public void handleException(BleException ex) {
        for(OnCommonDeviceListener listener : listeners) {
            if(listener != null) {
                listener.onExceptionNotified(this, ex);
            }
        }
    }

    @Override
    public void onConnectStateUpdated() {
        for(OnCommonDeviceListener listener : listeners) {
            if(listener != null) {
                listener.onStateUpdated(this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractDevice)) return false;
        AbstractDevice that = (AbstractDevice) o;
        return info.equals(that.info);
    }

    @Override
    public int hashCode() {
        return (info != null) ? info.hashCode() : 0;
    }

}
