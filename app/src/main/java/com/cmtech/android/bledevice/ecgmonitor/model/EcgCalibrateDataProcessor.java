package com.cmtech.android.bledevice.ecgmonitor.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EcgCalibrateDataProcessor: 心电标定数据处理器
 * Created by Chenm, 2018-12-27
 */

public class EcgCalibrateDataProcessor {
    public interface ICalibrateValueUpdatedListener {
        void onUpdateCalibrateValue(int calibrateValue); // 更新标定值
    }

    private final List<Integer> calibrationData; // 用于保存标定用的数据

    private final int sampleRate; // 采样率

    private ICalibrateValueUpdatedListener listener; // 标定值监听器

    EcgCalibrateDataProcessor(int sampleRate, ICalibrateValueUpdatedListener listener) {
        this.sampleRate = sampleRate;
        this.listener = listener;
        calibrationData = new ArrayList<>(2 * sampleRate);
    }

    // 处理标定数据
    public synchronized void process(int calibrateData) {
        // 采集1个周期的定标信号
        if (calibrationData.size() < sampleRate) {
            calibrationData.add(calibrateData);
        }
        else {
            int value = calculateCalibration(calibrationData); // 计算得到实际定标值
            if(listener != null) listener.onUpdateCalibrateValue(value);
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

    public void close() {
        listener = null;
    }
}
