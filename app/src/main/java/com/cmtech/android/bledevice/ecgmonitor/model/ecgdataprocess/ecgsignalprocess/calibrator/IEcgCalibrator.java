package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator;

/**
 * IEcgCalibrator: 心电信号定标器接口，用于定标（归一化）信号值
 * Created by bme on 2018/12/06.
 */

public interface IEcgCalibrator {
    int process(int data); // 对输入信号值做定标处理

    void setValue1mVBeforeCalibration(int before); // 设置定标值
}
