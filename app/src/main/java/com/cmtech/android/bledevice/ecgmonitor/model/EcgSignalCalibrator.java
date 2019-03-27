package com.cmtech.android.bledevice.ecgmonitor.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EcgSignalCalibrator: 心电信号定标器
 * Created by Chenm, 2018-12-27
 */

public class EcgSignalCalibrator {
    public interface ICalibrateValueUpdatedListener {
        void onCalibrateValueUpdated(int calibrateValue); // 更新标定值
    }

    private final List<Integer> calibrationData = new ArrayList<>(250); // 用于保存标定用的数据

    private final int sampleRate; // 采样率

    private ICalibrateValueUpdatedListener listener; // 标定值监听器

    public EcgSignalCalibrator(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    // 处理标定数据
    public void process(int calibrateData) {
        // 采集1个周期的定标信号
        if (calibrationData.size() < sampleRate) {
            calibrationData.add(calibrateData);
        }
        else {
            int value = calculateCalibration(calibrationData); // 计算得到实际定标值
            if(listener != null) listener.onCalibrateValueUpdated(value);
            calibrationData.clear();
        }
    }

    // 计算定标值
    private int calculateCalibration(List<Integer> data) {
        Integer[] arr = data.toArray(new Integer[0]);
        Arrays.sort(arr); // 从小到大排序
        int len = (arr.length-10)/2; // 去掉10个中间大小的数据
        int sum1 = 0;
        int sum2 = 0;
        for(int i = 0; i < len; i++) {
            sum1 += arr[i];
            sum2 += arr[arr.length-i-1];
        }
        return (sum2-sum1)/2/len;
    }

    public void setCalibrateValueUpdatedListener(ICalibrateValueUpdatedListener listener) {
        this.listener = listener;
    }

    public void removeCalibrateValueUpdatedListener() {
        listener = null;
    }
}
