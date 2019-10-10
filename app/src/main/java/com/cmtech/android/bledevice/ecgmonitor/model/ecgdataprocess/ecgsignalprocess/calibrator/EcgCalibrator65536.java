package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator;

/**
 * EcgCalibrator65536: 心电信号定标处理器，定标后的基准值为65536，可以利用这一点简化运算
 * Created by bme on 2018/12/06.
 */

public class EcgCalibrator65536 extends EcgCalibrator {
    public EcgCalibrator65536(int value1mVBeforeCalibration) {
        super(value1mVBeforeCalibration);
    }

    // 乘以65536可以用右移16位实现
    @Override
    public int process(int data) {
        return (data<<16)/ value1mVBeforeCalibration;
    }
}
