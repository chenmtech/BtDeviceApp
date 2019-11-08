package com.cmtech.android.bledevice.ecgmonitor.process.signal.calibrator;


/**
  *
  * ClassName:      EcgCalibrator
  * Description:    心电信号定标器，根据1mV定标值对心电数据进行定标（归一化）处理
  * Author:         chenm
  * CreateDate:     2018-12-06 07:23
  * UpdateUser:     chenm
  * UpdateDate:     2019-07-03 07:23
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgCalibrator implements IEcgCalibrator {
    int value1mV; // 定标前的1mV值
    private int value1mVAfterCalibration; // 定标后的1mV的值

    public EcgCalibrator(int value1mV, int value1mVAfterCalibration) {
        this.value1mV = value1mV;
        this.value1mVAfterCalibration = value1mVAfterCalibration;
    }

    @Override
    public void reset(int value1mV, int value1mVAfterCalibration) {
        this.value1mV = value1mV;
        this.value1mVAfterCalibration = value1mVAfterCalibration;
    }

    @Override
    public int calibrate(int data) {
        return data* value1mVAfterCalibration / value1mV;
    }
}
