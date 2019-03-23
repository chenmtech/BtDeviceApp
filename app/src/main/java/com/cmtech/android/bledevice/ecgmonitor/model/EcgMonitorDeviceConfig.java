package com.cmtech.android.bledevice.ecgmonitor.model;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.HR_HIGH_LIMIT;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.HR_LOW_LIMIT;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.WARN_WHEN_HR_ABNORMAL;

/**
 * EcgMonitorDeviceConfig: 心电带配置类
 * Created by bme on 2018/12/20.
 */

public class EcgMonitorDeviceConfig extends LitePalSupport implements Serializable {
    private final static long serialVersionUID = 1L;

    private int id; // id
    private String macAddress = ""; // mac地址
    private boolean warnWhenHrAbnormal = WARN_WHEN_HR_ABNORMAL; // hr异常时是否报警
    private int hrLowLimit = HR_LOW_LIMIT; // hr异常的下限
    private int hrHighLimit = HR_HIGH_LIMIT; // hr异常的上限
    private List<String> markerList = new ArrayList<>(Arrays.asList("标记1", "标记2", "标记3", "标记4"));

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

    public List<String> getMarkerList() {
        return markerList;
    }

    public void setMarkerList(List<String> markerList) {
        this.markerList = markerList;
    }
}
