package com.cmtech.android.btdevice.temphumid;

import java.util.Date;

public class TempHumidData {
    Date time;
    float temp;
    int humid;

    public TempHumidData(Date time, float temp, int humid) {
        this.time = time;
        this.temp = temp;
        this.humid = humid;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
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

    @Override
    public String toString() {
        return "TempHumidData{" +
                "time=" + time +
                ", temp=" + temp +
                ", humid=" + humid +
                '}';
    }
}
