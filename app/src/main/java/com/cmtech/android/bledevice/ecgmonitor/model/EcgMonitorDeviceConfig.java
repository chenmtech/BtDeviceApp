package com.cmtech.android.bledevice.ecgmonitor.model;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.HR_HIGH_LIMIT;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.HR_LOW_LIMIT;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.WARN_WHEN_HR_ABNORMAL;

public class EcgMonitorDeviceConfig extends LitePalSupport implements Serializable {
    private int id;
    private String macAddress = "";
    private boolean warnWhenHrAbnormal = WARN_WHEN_HR_ABNORMAL;
    private int hrLowLimit = HR_LOW_LIMIT;
    private int hrHighLimit = HR_HIGH_LIMIT;

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
