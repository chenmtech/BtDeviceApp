package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CalibrateDataProcessor {
    private final List<Integer> calibrationData = new ArrayList<>(250); // 用于保存标定用的数据
    private int sampleRate;
    private ICalibrateValueObserver observer;

    public CalibrateDataProcessor(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void process(int calibrateData) {
        // 采集1个周期的定标信号
        if (calibrationData.size() < sampleRate) {
            calibrationData.add(calibrateData);
        }
        else {
            // 计算得到实际1mV定标值
            int value1mVBeforeCalibrate = calculateCalibration(calibrationData);
            calibrationData.clear();
            notifyObserver(value1mVBeforeCalibrate);
        }
    }

    private int calculateCalibration(List<Integer> data) {
        Integer[] arr = data.toArray(new Integer[0]);
        Arrays.sort(arr);

        int len = (arr.length-10)/2;
        int sum1 = 0;
        int sum2 = 0;
        for(int i = 0; i < len; i++) {
            sum1 += arr[i];
            sum2 += arr[arr.length-i-1];
        }
        return (sum2-sum1)/2/len;
    }

    public void registerObserver(ICalibrateValueObserver observer) {
        this.observer = observer;
    }
    public void notifyObserver(int calibrateValue) {
        observer.updateCalibrateValue(calibrateValue);
    }

    public void removeObserver() {
        observer = null;
    }
}
