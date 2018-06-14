package com.cmtech.android.btdevice.temphumid;

import android.support.annotation.NonNull;

import com.cmtech.android.btdeviceapp.util.ByteUtil;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class TempHumidData implements Comparable{
    Calendar time;
    float temp;
    int humid;

    public TempHumidData(Calendar time, float temp, int humid) {
        this.time = (Calendar)time.clone();
        this.temp = temp;
        this.humid = humid;
    }

    public TempHumidData(Calendar time, byte[] data) {
        this.time = (Calendar)time.clone();
        byte[] buf = Arrays.copyOfRange(data, 0, 4);
        humid = (int) ByteUtil.getFloat(buf);
        buf = Arrays.copyOfRange(data, 4, 8);
        temp = ByteUtil.getFloat(buf);
    }

    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = (Calendar)time.clone();
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
        return DateFormat.getDateTimeInstance().format(time.getTime()) +
                ": temp=" + temp +
                ", humid=" + humid;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return time.compareTo(((TempHumidData)o).time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TempHumidData that = (TempHumidData) o;
        //return Objects.equals(time, that.time);
        return time.equals(that.time);
    }

    @Override
    public int hashCode() {
        return time.hashCode();
        //return Objects.hash(time);
    }
}
