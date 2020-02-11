package com.cmtech.android.bledevice.temphumid.model;

import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class BleTempHumidData implements Comparable{
    private long time;
    private int temp;
    private int humid;

    public BleTempHumidData(long time, int temp, int humid) {
        this.time = time;
        this.temp = temp;
        this.humid = humid;
    }

    public BleTempHumidData(TempHumidHistoryData data) {
        time = data.getTimeInMillis();
        temp = (int)data.getTemp();
        humid = data.getHumid();
    }

    public long getTime() {
        return time;
    }

    public int getTemp() {
        return temp;
    }

    public int getHumid() {
        return humid;
    }

    public float calculateHeatIndex() {
        float t = getTemp()/100.0f;
        float rh = getHumid()/100.0f;
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
        return "Time=" + DateTimeUtil.timeToShortString(time) +
                ": temp=" + temp +
                ", humid=" + humid;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return (int)(time - ((BleTempHumidData)o).time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BleTempHumidData that = (BleTempHumidData) o;
        return time == that.time;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(time).hashCode();
    }
}
