package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgfilter;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.IEcgProcessor;

/**
 * IEcgFilter: Ecg信号滤波器接口
 * Created by Chenm, 2018-11-29
 */

public interface IEcgFilter extends IEcgProcessor{
    // 滤波
    double filter(double ecgSignal);
}
