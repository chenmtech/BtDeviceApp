package com.cmtech.android.bledevice.ecg.process.signal.filter;

/**
 * IEcgFilter: Ecg信号滤波器接口
 * Created by Chenm, 2018-11-29
 */

public interface IEcgFilter{
    double filter(double ecgSignal); // 滤波处理
    void reset(int sampleRate); // 重置采样频率
}
