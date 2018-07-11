package com.cmtech.android.btdevice.temphumid;

import org.litepal.crud.LitePalSupport;

import java.util.Calendar;

public class TempHumidHistoryData extends LitePalSupport {
    // 数据库保存的字段
    // id
    private int id;

    // 设备mac地址
    private String macAddress;

    private Calendar time;

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

    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = time;
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
