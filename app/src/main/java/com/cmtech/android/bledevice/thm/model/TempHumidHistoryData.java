package com.cmtech.android.bledevice.thm.model;

import org.litepal.crud.LitePalSupport;

public class TempHumidHistoryData extends LitePalSupport {
    private int id;
    private String macAddress; // 设备mac地址
    private long timeInMillis;
    private float temp;
    private int humid;

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
    public long getTimeInMillis() {
        return timeInMillis;
    }
    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }
    public float getTemp() {
        return temp;
    }
    public void setTemp(float temp) {
        this.temp = temp;
    }
    public int getHumid() {
        return humid;
    }
    public void setHumid(int humid) {
        this.humid = humid;
    }
}
