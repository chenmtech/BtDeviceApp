package com.cmtech.android.bledevice.ecgmonitor.model;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class EcgMonitorDeviceConfig extends LitePalSupport implements Serializable {
    private int id;
    private String macAddress = "";
    private boolean warnWhenDisconnect = true;
    private boolean warnWhenHrAbnormal = true;
    private int hrLowLimit = 50;
    private int hrHighLimit = 100;

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

    public boolean isWarnWhenDisconnect() {
        return warnWhenDisconnect;
    }

    public void setWarnWhenDisconnect(boolean warnWhenDisconnect) {
        this.warnWhenDisconnect = warnWhenDisconnect;
    }

    public boolean isWarnWhenHrAbnormal() {
        return warnWhenHrAbnormal;
    }

    public void setWarnWhenHrAbnormal(boolean warnWhenHrAbnormal) {
        this.warnWhenHrAbnormal = warnWhenHrAbnormal;
    }

    public int getHrLowLimit() {
        return hrLowLimit;
    }

    public void setHrLowLimit(int hrLowLimit) {
        this.hrLowLimit = hrLowLimit;
    }

    public int getHrHighLimit() {
        return hrHighLimit;
    }

    public void setHrHighLimit(int hrHighLimit) {
        this.hrHighLimit = hrHighLimit;
    }
}
