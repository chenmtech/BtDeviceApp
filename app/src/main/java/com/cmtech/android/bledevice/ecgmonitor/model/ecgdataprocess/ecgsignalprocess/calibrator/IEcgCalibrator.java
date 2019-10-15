package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator;

/**
 * IEcgCalibrator: 心电信号定标器接口，用于定标（归一化）信号值
 * Created by bme on 2018/12/06.
 */

public interface IEcgCalibrator {
    int STANDARD_VALUE_1MV_AFTER_CALIBRATION = 65535; // 定标后标准的1mV值
    int calibrate(int data); // 对数据做定标处理
    void reset(int value1mV, int value1mVAfterCalibration); // 重置1mV值
}
