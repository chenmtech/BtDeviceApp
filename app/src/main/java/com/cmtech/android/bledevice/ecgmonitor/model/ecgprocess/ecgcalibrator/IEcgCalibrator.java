package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrator;

/**
 * IEcgCalibrator: 心电信号定标器接口，用于定标（归一化）信号值
 * Created by bme on 2018/12/06.
 */

public interface IEcgCalibrator {
    // 对输入信号值做定标处理
    int process(int data);
}
