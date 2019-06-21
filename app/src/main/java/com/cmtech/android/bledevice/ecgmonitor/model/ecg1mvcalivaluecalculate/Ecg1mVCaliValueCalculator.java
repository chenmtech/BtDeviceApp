package com.cmtech.android.bledevice.ecgmonitor.model.ecg1mvcalivaluecalculate;

import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ecg1mVCaliValueCalculator: 心电标定数据处理器
 * Created by Chenm, 2018-12-27
 */

/**
  *
  * ClassName:      Ecg1mVCaliValueCalculator
  * Description:    心电1mV标定值计算器
  * Author:         chenm
  * CreateDate:     2019-06-15 08:11
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 08:11
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class Ecg1mVCaliValueCalculator {

    private final List<Integer> calibrationData; // 用于保存标定用的数据

    private final int sampleRate; // 采样率

    private On1mVCaliValueListener listener; // 标定值监听器

    private boolean done = false;

    public Ecg1mVCaliValueCalculator(int sampleRate, On1mVCaliValueListener listener) {
        this.sampleRate = sampleRate;

        this.listener = listener;

        calibrationData = new ArrayList<>(2 * sampleRate);
    }

    // 处理标定数据
    public void process(int calibrateData) {
        if(done) return;

        // 采集1个周期的定标信号
        if (calibrationData.size() < sampleRate) {
            calibrationData.add(calibrateData);
        }
        else {
            int value = calculateCalibration(calibrationData); // 计算得到实际定标值

            ViseLog.e(calibrationData.toString() + " " + value);

            if(listener != null) listener.on1mVCaliValueUpdated(value);

            calibrationData.clear();

            done = true;
        }
    }

    // 计算定标值
    private int calculateCalibration(List<Integer> data) {
        Integer[] arr = data.toArray(new Integer[0]);

        Arrays.sort(arr); // 从小到大排序

        int halfLen = (arr.length-10)/2; // 去掉10个中间的数据

        int sum1 = 0;

        int sum2 = 0;

        for(int i = 0; i < halfLen; i++) {
            sum1 += arr[i];

            sum2 += arr[arr.length-i-1];
        }
        return (sum2-sum1)/2/halfLen;
    }

    public void close() {
        listener = null;
    }
}
