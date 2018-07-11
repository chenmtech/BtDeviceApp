package com.cmtech.android.btdevice.temphumid;

import android.support.annotation.NonNull;

import com.cmtech.android.btdeviceapp.util.ByteUtil;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class TempHumidData implements Comparable{
    private Calendar time;
    private float temp;
    private int humid;

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

    public float computeHeatIndex() {
        float t = getTemp();
        float rh = getHumid();
        t = t*1.8f+32.0f;
        float index = (float)((16.923 + (0.185212 * t) + (5.37941 * rh) - (0.100254 * t * rh) +
                (0.00941695 * (t * t)) + (0.00728898 * (rh * rh)) +
                (0.000345372 * (t * t * rh)) - (0.000814971 * (t * rh * rh)) +
                (0.0000102102 * (t * t * rh * rh)) - (0.000038646 * (t * t * t)) + (0.0000291583 *
                (rh * rh * rh)) + (0.00000142721 * (t * t * t * rh)) +
                (0.000000197483 * (t * rh * rh * rh)) - (0.0000000218429 * (t * t * t * rh * rh)) +
                0.000000000843296 * (t * t * rh * rh * rh)) -
                (0.0000000000481975 * (t * t * t * rh * rh * rh)));
        index = (index-32)/1.8f;
        return index;
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
        return time.equals(that.time);
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }
}
