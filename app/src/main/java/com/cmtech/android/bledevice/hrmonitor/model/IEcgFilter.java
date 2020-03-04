package com.cmtech.android.bledevice.hrmonitor.model;

/**
 * IEcgFilter: Ecg信号滤波器接口
 * Created by Chenm, 2018-11-29
 */

public interface IEcgFilter {
    double filter(double ecgSignal); // filter process
    void reset(int sampleRate); // reset with different sample rate
}
