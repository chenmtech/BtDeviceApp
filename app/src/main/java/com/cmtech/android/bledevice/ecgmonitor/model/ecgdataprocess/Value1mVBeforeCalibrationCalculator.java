package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.ecgcalibrator.EcgCalibrator65536;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Value1mVBeforeCalibrationCalculator: 心电标定数据处理器
 * Created by Chenm, 2018-12-27
 */

/**
  *
  * ClassName:      Value1mVBeforeCalibrationCalculator
  * Description:    定标前1mV值计算器
  * Author:         chenm
  * CreateDate:     2019-06-15 08:11
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 08:11
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class Value1mVBeforeCalibrationCalculator {
    private final EcgMonitorDevice device;
    private int sampleRate; // 采样率
    private List<Integer> calibrationData; // 用于保存标定用的数据
    private final EcgCalibrator65536 calibrator;
    private boolean done = false;

    public Value1mVBeforeCalibrationCalculator(EcgMonitorDevice device) {
        this.device = device;
        this.sampleRate = device.getSampleRate();
        calibrator = new EcgCalibrator65536(device.getValue1mVBeforeCalibration());
        calibrationData = new ArrayList<>(2 * this.sampleRate);
    }

    public void update() {
        sampleRate = device.getSampleRate();
        calibrator.setValue1mVBeforeCalibration(device.getValue1mVBeforeCalibration());
        calibrationData = new ArrayList<>(2*sampleRate);
        done = false;
    }

    // 处理标定数据
    public void process(int calibrateData) {
        if(done) return;

        // 采集1个周期的定标信号
        if (calibrationData.size() < sampleRate) {
            calibrationData.add(calibrateData);
        } else {
            device.stopDataSampling();
            int value1mVBeforeCalibration = calculateValue1mVBeforeCalibration(calibrationData); // 计算得到实际定标前1mV值
            device.updateValue1mVBeforeCalibration(value1mVBeforeCalibration);
            ViseLog.e(calibrationData.toString() + " " + value1mVBeforeCalibration);
            calibrationData.clear();
            done = true;
        }

        device.updateSignalValue(calibrator.process(calibrateData));
    }

    // 计算定标前1mV值
    private int calculateValue1mVBeforeCalibration(List<Integer> data) {
        Integer[] arr = data.toArray(new Integer[0]);
        Arrays.sort(arr); // 从小到大排序
        int halfLen = (arr.length - 20)/2; // 去掉20个中间的数据
        int sum1 = 0;
        int sum2 = 0;
        for(int i = 0; i < halfLen; i++) {
            sum1 += arr[i];
            sum2 += arr[arr.length-i-1];
        }
        return (sum2-sum1)/2/halfLen;
    }
}
