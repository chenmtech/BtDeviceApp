package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator.IEcgCalibrator;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Value1mVDetector: 心电标定数据处理器
 * Created by Chenm, 2018-12-27
 */

/**
  *
  * ClassName:      Value1mVDetector
  * Description:    1mV数据值检测器
  * Author:         chenm
  * CreateDate:     2019-06-15 08:11
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 08:11
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class Value1mVDetector {
    private final EcgMonitorDevice device;
    private int sampleRate; // 采样率
    private List<Integer> calibrationData; // 用于保存标定用的数据
    private final EcgCalibrator65536 calibrator;
    private boolean done = false;

    public Value1mVDetector(EcgMonitorDevice device) {
        this.device = device;
        this.sampleRate = device.getSampleRate();
        calibrator = new EcgCalibrator65536(device.getValue1mV());
        calibrationData = new ArrayList<>(2 * this.sampleRate);
    }

    public void reset() {
        sampleRate = device.getSampleRate();
        calibrator.reset(device.getValue1mV(), IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION);
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
            int value1mV = calculateValue1mV(calibrationData); // 计算得到实际定标前1mV值
            device.setValue1mV(value1mV);
            ViseLog.e(calibrationData.toString() + " " + value1mV);
            calibrationData.clear();
            done = true;
        }

        device.updateSignalValue(calibrator.calibrate(calibrateData));
    }

    // 计算定标前1mV值
    private int calculateValue1mV(List<Integer> data) {
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