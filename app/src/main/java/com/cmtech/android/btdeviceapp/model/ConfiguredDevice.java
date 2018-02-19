package com.cmtech.android.btdeviceapp.model;

import org.litepal.crud.DataSupport;

/**
 * Created by bme on 2018/2/19.
 */

public class ConfiguredDevice extends DataSupport{
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
}
