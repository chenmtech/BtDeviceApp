package com.cmtech.android.bledevice.ecgmonitor.model.ecgfilter;

/**
 * IEcgFilter: Ecg信号滤波器接口
 * Created by Chenm, 2018-11-29
 */

public interface IEcgFilter {
    // Ecg滤波器的初始化
    void init(int sampleRate);
    // 滤波
    double filter(double ecgSignal);
}
