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
        notifyDeviceObservers();
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

    List<IConfiguredDeviceObersver> obersvers = new ArrayList<>();

    public ConnectState getConnectState() {return connectState;}

    public String getConnectStateString() {
        String rtn = "等待连接";
        switch (connectState) {
            case CONNECT_INIT:
                rtn = "等待连接";
                break;
            case CONNECT_PROCESS:
                rtn = "连接中...";
                break;
            case CONNECT_DISCONNECT:
                rtn = "连接断开";
                break;
            case CONNECT_FAILURE:
                rtn = "连接错误";
                break;
            case CONNECT_SUCCESS:
                rtn = "已连接";
                break;
            default:
                break;
        }
        return rtn;
    }

    public void setConnectState(ConnectState state) {this.connectState = state; notifyDeviceObservers();}

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

    public interface IConfiguredDeviceObersver {
        void updateDeviceInfo(ConfiguredDevice device);
    }

    public void registerDeviceObserver(IConfiguredDeviceObersver obersver) {
        obersvers.add(obersver);
    }

    public void removerDeviceObserver(IConfiguredDeviceObersver obersver) {
        int index = obersvers.indexOf(obersver);
        if(index >= 0) {
            obersvers.remove(index);
        }
    }

    private void notifyDeviceObservers() {
        for(IConfiguredDeviceObersver obersver : obersvers) {
            obersver.updateDeviceInfo(this);
        }
    }

    public void connect() {
        setConnectState(ConnectState.CONNECT_PROCESS);
        MyApplication.getViseBle().connectByMac(macAddress, connectCallback);
    }
}
