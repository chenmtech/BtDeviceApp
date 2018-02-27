package com.cmtech.android.btdeviceapp.model;

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.btdeviceapp.MyApplication;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bme on 2018/2/19.
 */

public class ConfiguredDevice extends DataSupport implements Serializable {
    // 数据库会保存的字段
    private int id;

    private String macAddress;

    private String nickName;

    private boolean isAutoConnected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isAutoConnected() {
        return isAutoConnected;
    }

    public void setAutoConnected(boolean autoConnected) {
        isAutoConnected = autoConnected;
    }


    // 数据库不会保存的变量
    ConnectState connectState = ConnectState.CONNECT_INIT;

    IConnectCallback connectCallback = new ConfiguredDeviceConnectCallback();

    DeviceMirror deviceMirror = null;

    List<IConnectStateObersver> obersvers = new ArrayList<>();

    public ConnectState getConnectState() {return connectState;}

    public void setConnectState(ConnectState state) {this.connectState = state; notifyConnectStateObservers();}

    public class ConfiguredDeviceConnectCallback implements IConnectCallback {
        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();
            if(deviceMirrorPool.isContainDevice(deviceMirror)) {
                setConnectState(ConnectState.CONNECT_SUCCESS);
                ConfiguredDevice.this.deviceMirror = deviceMirror;
            }
        }

        @Override
        public void onConnectFailure(BleException exception) {
            setConnectState(ConnectState.CONNECT_FAILURE);
        }

        @Override
        public void onDisconnect(boolean isActive) {
            setConnectState(ConnectState.CONNECT_DISCONNECT);
        }
    }

    public interface IConnectStateObersver {
        void updateConnectState(ConfiguredDevice device, ConnectState state);
    }

    public void registerConnectStateObserver(IConnectStateObersver obersver) {
        obersvers.add(obersver);
    }

    public void removerConnectStateObserver(IConnectStateObersver obersver) {
        int index = obersvers.indexOf(obersver);
        if(index >= 0) {
            obersvers.remove(index);
        }
    }

    private void notifyConnectStateObservers() {
        for(IConnectStateObersver obersver : obersvers) {
            obersver.updateConnectState(this, connectState);
        }
    }

    public void connect() {
        setConnectState(ConnectState.CONNECT_PROCESS);
        MyApplication.getViseBle().connectByMac(macAddress, connectCallback);
    }
}
