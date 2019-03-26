package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor.INVALID_HR;

/**
 * EcgHrProcessor: 心率处理类
 * Created by Chenm, 2018-12-07
 */

public class EcgHrProcessor implements IEcgHrProcessor {
    private final List<Integer> hrArray = new ArrayList<>();

    public class EcgHrStatistics {
        public double[] normHistogram;
        public int totalBeats;
        public int hrMax;
        public int hrAverage;
    }

    public EcgHrProcessor() {

    }

    public EcgHrStatistics getHrStatistics(int barNum) {
        List<Integer> hrAverage = filter(10);

        double[] normHistogram = getNormHistogram(barNum, hrAverage);
        if(normHistogram == null) return null;
        EcgHrStatistics statistics = new EcgHrStatistics();
        statistics.normHistogram = normHistogram;
        statistics.totalBeats = getTotalBeats();
        statistics.hrMax = Collections.max(hrAverage);
        statistics.hrAverage =
    }

    // 重置心率数据
    public void clear() {
        hrArray.clear();
    }

    // 获取直方图
    public int[] getHistogram(int barNum) {
        List<Integer> hrAverage = filter(10);

        return getHistogram(barNum, hrAverage);
    }

    public int[] getHistogram(int barNum, List<Integer> hrArray) {
        if(hrArray.size() < 1) return null;

        int minValue = Collections.min(hrArray);
        int maxValue = Collections.max(hrArray);
        int interval = (maxValue-minValue)/barNum+1;

        int[] histogram = new int[barNum];
        int num = 0;
        for(int hr : hrArray) {
            num = (hr-minValue)/interval;
            int i = (num >= barNum) ? barNum-1 : num;
            histogram[i]++;
        }

        return histogram;
    }

    // 获取归一化直方图
    public double[] getNormHistogram(int barNum, List<Integer> hrArray) {
        int[] histogram = getHistogram(barNum, hrArray);
        if(histogram == null) return null;
        double[] norm = new double[histogram.length];
        int sum = 0;
        for(int num : histogram) sum += num;
        for(int i = 0; i < histogram.length; i++) {
            norm[i] = ((double)histogram[i])/sum;
        }
        return norm;
    }

    // 获取总的心跳次数
    public int getTotalBeats() {
        return hrArray.size();
    }

    @Override
    public void process(int hr) {
        if(hr != INVALID_HR) {
            hrArray.add(hr);
        }
    }

    private List<Integer> filter(int second) {
        List<Integer> hrAverage = new ArrayList<>();
        double sum = 0.0;
        int num = 0;
        double period = 0.0;
        for(int hr : hrArray) {
            sum += hr;
            num++;
            period += 60.0/hr;
            if(period >= second) {
                hrAverage.add((int)sum/num);
                period -= second;
                sum = 0;
                num = 0;
            }
        }
        return hrAverage;
    }
}
