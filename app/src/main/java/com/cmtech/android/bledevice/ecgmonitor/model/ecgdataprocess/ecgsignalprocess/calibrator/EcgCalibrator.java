package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator;


import static com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice.VALUE_1MV_AFTER_CALIBRATION;

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
    int value1mVBeforeCalibration; // 定标前的1mV的值
    private int value1mVAfterCalibration; // 定标后的1mV的值

    public EcgCalibrator(int value1mVBeforeCalibration, int value1mVAfterCalibration) {
        this.value1mVBeforeCalibration = value1mVBeforeCalibration;
        this.value1mVAfterCalibration = value1mVAfterCalibration;
    }

    EcgCalibrator(int value1mVBeforeCalibration) {
        this(value1mVBeforeCalibration, VALUE_1MV_AFTER_CALIBRATION);
    }

    @Override
    public void setValue1mVBeforeCalibration(int before) {
        value1mVBeforeCalibration = before;
    }

    @Override
    public int process(int data) {
        return data* value1mVAfterCalibration / value1mVBeforeCalibration;
    }
}
