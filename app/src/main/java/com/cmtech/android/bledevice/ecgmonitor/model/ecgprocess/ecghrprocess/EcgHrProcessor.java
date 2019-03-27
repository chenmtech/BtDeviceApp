package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor.INVALID_HR;

/**
 * EcgHrProcessor: 心率处理类
 * Created by Chenm, 2018-12-07
 */

public class EcgHrProcessor implements IEcgHrProcessor {
    // 心率统计信息更新监听器
    public interface IEcgHrStatisticsUpdatedListener {
        void onEcgHrStatisticsUpdated(List<Integer> hrAverage, double[] normHistogram, int maxHr, int averageHr);
    }

    private final List<Integer> hrList = new ArrayList<>();
    private List<Integer> hrAverage = new ArrayList<>();

    private double[] normHistogram;

    private int maxHr;
    private int averageHr;

    private IEcgHrStatisticsUpdatedListener listener;

    public EcgHrProcessor() {

    }

    public List<Integer> getHrList() {
        return hrList;
    }

    // 更新心率统计分析
    public void updateHrStatistics(int secondInAverageFilter, int barNumInHistogram) {
        hrAverage = averageFilter(secondInAverageFilter);
        normHistogram = makeNormHistogram(barNumInHistogram, hrAverage);
        if(!hrAverage.isEmpty()) {
            maxHr = Collections.max(hrAverage);
            int hrSum = 0;
            for (int hr : hrAverage) {
                hrSum += hr;
            }
            averageHr = hrSum / hrAverage.size();
        } else {
            maxHr = 0;
            averageHr = 0;
        }
        if(listener != null)
            listener.onEcgHrStatisticsUpdated(hrAverage, normHistogram, maxHr, averageHr);
    }

    // 重置心率数据
    public void clear() {
        hrList.clear();
        hrAverage.clear();
        normHistogram = null;
        maxHr = 0;
        averageHr = 0;
        if(listener != null)
            listener.onEcgHrStatisticsUpdated(hrAverage, normHistogram, maxHr, averageHr);
    }


    // 产生归一化直方图
    private double[] makeNormHistogram(int barNum, List<Integer> hrArray) {
        int[] histogram = makeHistogram(barNum, hrArray);
        if(histogram == null) return null;
        double[] norm = new double[histogram.length];
        int sum = 0;
        for(int num : histogram) sum += num;
        for(int i = 0; i < histogram.length; i++) {
            norm[i] = ((double)histogram[i])/sum;
        }
        return norm;
    }

    private int[] makeHistogram(int barNumInHistogram, List<Integer> hrArray) {
        if(hrArray.size() < 1) return null;

        int minValue = Collections.min(hrArray);
        int maxValue = Collections.max(hrArray);
        int interval = (maxValue-minValue)/barNumInHistogram+1;

        int[] histogram = new int[barNumInHistogram];
        int num = 0;
        for(int hr : hrArray) {
            num = (hr-minValue)/interval;
            int i = (num >= barNumInHistogram) ? barNumInHistogram-1 : num;
            histogram[i]++;
        }

        return histogram;
    }

    @Override
    public void process(int hr) {
        if(hr != INVALID_HR) {
            hrList.add(hr);
        }
    }

    private List<Integer> averageFilter(int second) {
        List<Integer> hrAverage = new ArrayList<>();
        double sum = 0.0;
        int num = 0;
        double period = 0.0;
        for(int hr : hrList) {
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

    public void setEcgHrStatisticsUpdatedListener(IEcgHrStatisticsUpdatedListener listener) {
        this.listener = listener;
    }

    public void removeEcgHrStatisticsUpdatedListener() {
        listener = null;
    }

}
