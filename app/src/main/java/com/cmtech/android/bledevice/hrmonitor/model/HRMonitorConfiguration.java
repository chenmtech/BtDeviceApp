package com.cmtech.android.bledevice.hrmonitor.model;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      HrConfiguration
 * Description:    configuration of hr monitor device
 * Author:         chenm
 * CreateDate:     2020/3/19 上午6:26
 * UpdateUser:     chenm
 * UpdateDate:     2020/3/19 上午6:26
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HRMonitorConfiguration extends LitePalSupport implements Serializable {
    private final static long serialVersionUID = 1L;
    public static final int DEFAULT_HR_LOW_LIMIT = 50;
    public static final int DEFAULT_HR_HIGH_LIMIT = 180;
    public static final boolean DEFAULT_HR_WARN = true;
    public static final int DEFAULT_HR_SPEAK_PERIOD = 5; // unit: m
    public static final boolean DEFAULT_HR_SPEAK = true;

    private int id; // id
    private String address = ""; // mac address
    private int hrLow = DEFAULT_HR_LOW_LIMIT; // hr exception low limit
    private int hrHigh = DEFAULT_HR_HIGH_LIMIT; // hr exception high limit
    private boolean isWarn = DEFAULT_HR_WARN; // is warn when hr exception occurred
    private int speakPeriod = DEFAULT_HR_SPEAK_PERIOD;
    private boolean isSpeak = DEFAULT_HR_SPEAK;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public boolean isWarn() {
        return isWarn;
    }
    public void setWarn(boolean warn) {
        this.isWarn = warn;
    }
    public int getHrLow() {
        return hrLow;
    }
    public void setHrLow(int hrLow) {
        this.hrLow = hrLow;
    }
    public int getHrHigh() {
        return hrHigh;
    }
    public void setHrHigh(int hrHigh) {
        this.hrHigh = hrHigh;
    }
    public int getSpeakPeriod() {
        return speakPeriod;
    }
    public void setSpeakPeriod(int speakPeriod) {
        this.speakPeriod = speakPeriod;
    }
    public boolean isSpeak() {
        return isSpeak;
    }
    public void setSpeak(boolean isSpeak) {
        this.isSpeak = isSpeak;
    }

    public void copyFrom(HRMonitorConfiguration config) {
        isWarn = config.isWarn;
        hrLow = config.hrLow;
        hrHigh = config.hrHigh;
        speakPeriod = config.speakPeriod;
        isSpeak = config.isSpeak;
    }
}
