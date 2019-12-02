package com.cmtech.android.bledevice.ecg.process.signal.calibrator;

/**
 * EcgCalibrator65536: 心电信号定标处理器，定标后1mV值为65536，可以利用这一点简化运算
 * Created by bme on 2018/12/06.
 */

public class EcgCalibrator65536 extends EcgCalibrator {
    public EcgCalibrator65536(int value1mV) {
        super(value1mV, STANDARD_VALUE_1MV_AFTER_CALIBRATION);
    }

    // 乘以65536可以用右移16位实现
    @Override
    public int calibrate(int data) {
        return (data<<16)/ value1mV;
    }
}
