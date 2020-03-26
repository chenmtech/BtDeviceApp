package com.cmtech.android.ble.core;

import android.content.Context;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.OtherException;
import com.vise.log.ViseLog;

import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.ble.core.DeviceState.CONNECT;
import static com.cmtech.android.ble.core.DeviceState.DISCONNECT;
import static com.cmtech.android.ble.core.DeviceState.FAILURE;

public abstract class AbstractDevice implements IDevice{
    private Context context; // context
    private final DeviceInfo registerInfo; // connCallback register information
    private int battery; // battery level
    private final List<OnDeviceListener> listeners; // connCallback listeners
    protected final IConnector connector; // connector

    public AbstractDevice(DeviceInfo registerInfo) {
        if(registerInfo == null) {
            throw new NullPointerException("The register info is null.");
        }
        this.registerInfo = registerInfo;
        if(registerInfo.isLocal()) {
            connector = new BleConnector(this.getAddress(), this);
        } else {
            connector = new WebConnector(this.getAddress(), this);
        }
        listeners = new LinkedList<>();
        battery = INVALID_BATTERY;
    }

    @Override
    public DeviceInfo getRegisterInfo() {
        return registerInfo;
    }
    @Override
    public void updateRegisterInfo(DeviceInfo registerInfo) {
        this.registerInfo.update(registerInfo);
    }
    @Override
    public boolean isLocal() {
        return registerInfo.isLocal();
    }
    @Override
    public final String getAddress() {
        return registerInfo.getAddress();
    }
    @Override
    public String getUuidString() {
        return registerInfo.getUuid();
    }
    @Override
    public String getName() {
        return registerInfo.getName();
    }
    @Override
    public void setName(String name) {
        registerInfo.setName(name);
    }
    @Override
    public String getImagePath() {
        return registerInfo.getIcon();
    }
    @Override
    public int getBattery() {
        return battery;
    }
    @Override
    public void setBattery(final int battery) {
        if(this.battery != battery) {
            this.battery = battery;
            for (final OnDeviceListener listener : listeners) {
                if (listener != null) {
                    listener.onBatteryUpdated(this);
                }
            }
        }
    }
    @Override
    public final void addListener(OnDeviceListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    @Override
    public final void removeListener(OnDeviceListener listener) {
        listeners.remove(listener);
    }
    @Override
    public DeviceState getState() {
        return connector.getState();
    }

    @Override
    public void open(Context context) {
        if (context == null) {
            throw new NullPointerException("The context is null.");
        }
        this.context = context;

        if (connector.open(context) && registerInfo.isAutoConnect())
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
        for(OnDeviceListener listener : listeners) {
            if(listener != null) {
                listener.onExceptionNotified(this, ex);
            }
        }
    }

    @Override
    public void onConnectStateUpdated() {
        for(OnDeviceListener listener : listeners) {
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
        return registerInfo.equals(that.registerInfo);
    }

    @Override
    public int hashCode() {
        return (registerInfo != null) ? registerInfo.hashCode() : 0;
    }

}
