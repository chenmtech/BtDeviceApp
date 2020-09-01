package com.cmtech.android.bledevice.record;

import java.io.Serializable;

// HR histogram element
public class HrHistogramBar<T> implements Serializable {
    private final short minValue;
    private final short maxValue;
    private final String title; // histogram bar title string
    private T value; // histogram value

    HrHistogramBar(short minValue, short maxValue, String title, T value) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.title = title;
        this.value = value; // unit: s
    }

    short getMinValue() {
        return minValue;
    }
    public short getMaxValue() {
        return maxValue;
    }
    public String getTitle() {
        return title;
    }
    public T getValue() {
        return value;
    }
    public void setValue(T value) {
        this.value = value;
    }
}
