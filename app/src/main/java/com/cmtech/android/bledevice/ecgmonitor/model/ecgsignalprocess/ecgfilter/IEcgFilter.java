package com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess.ecgfilter;

/**
 * IEcgFilter: Ecg信号滤波器接口
 * Created by Chenm, 2018-11-29
 */

public interface IEcgFilter{
    // 滤波
    double filter(double ecgSignal);
}
