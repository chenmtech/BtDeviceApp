package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor.INVALID_HR;

/**
 * EcgHrRecorder: 心率记录仪，记录每次心率值，并实现心率的统计分析，发布心率信息
 *
 * Created by Chenm, 2018-12-07
 */

public class EcgHrRecorder implements IEcgHrProcessor {
    private static final int MIN_INTERVAL_IN_HISTOGRAM = 10;

    // 心率信息更新监听器
    public interface IEcgHrInfoUpdatedListener {
        void onUpdateEcgHrInfo(List<Integer> filteredHrList, List<HrHistogramElement<Float>> normHistogram, int maxHr, int averageHr);
    }

    private IEcgHrInfoUpdatedListener listener;

    private List<Integer> hrList = new ArrayList<>();

    private boolean isRecord = false;

    // 心率直方图的一个单元类
    public static class HrHistogramElement<T> {
        private int minValue;
        private int maxValue;
        private T histValue;

        HrHistogramElement(int minValue, int maxValue, T histValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.histValue = histValue;
        }

        public String getBarString() {
            return String.valueOf(minValue) + '-' + String.valueOf(maxValue);
        }

        public T getHistValue() {
            return histValue;
        }

        int getMinValue() {
            return minValue;
        }

        int getMaxValue() {
            return maxValue;
        }
    }

    public EcgHrRecorder() {

    }

    public EcgHrRecorder(List<Integer> hrList) {
        if(hrList != null)
            this.hrList = hrList;
    }

    public List<Integer> getHrList() {
        return hrList;
    }

    public void setHrList(List<Integer> hrList) {
        if(hrList == null) {
            this.hrList.clear();
        } else
            this.hrList = hrList;
    }

    public void setRecord(boolean record) {
        isRecord = record;
    }

    // 更新心率信息
    public void updateHrInfo(int secondInHrFilter, int barNumInHistogram) {
        List<Integer> hrFiltered = hrFilter(secondInHrFilter);

        List<HrHistogramElement<Float>> normHistogram = createNormHistogram(hrFiltered, barNumInHistogram);

        int maxHr = 0;
        int averageHr = 0;
        if(!hrFiltered.isEmpty()) {
            maxHr = Collections.max(hrFiltered);

            int hrSum = 0;
            for (int hr : hrFiltered) {
                hrSum += hr;
            }
            averageHr = hrSum / hrFiltered.size();
        }

        if(listener != null)
            listener.onUpdateEcgHrInfo(hrFiltered, normHistogram, maxHr, averageHr);
    }

    // 重置心率数据
    public void reset() {
        hrList.clear();
        if(listener != null)
            listener.onUpdateEcgHrInfo(null, null, 0, 0);
    }


    // 产生归一化直方图
    private List<HrHistogramElement<Float>> createNormHistogram(List<Integer> hrList, int barNum) {
        List<HrHistogramElement<Integer>> histogram = createHistogram(hrList, barNum);
        if(histogram == null) return null;

        int sum = 0;
        for(HrHistogramElement<Integer> ele : histogram) {
            sum += ele.getHistValue();
        }

        List<HrHistogramElement<Float>> normHist = new ArrayList<>();
        for(HrHistogramElement<Integer> ele : histogram) {
            HrHistogramElement<Float> tmp = new HrHistogramElement<>(ele.getMinValue(), ele.getMaxValue(), ((float)ele.getHistValue())/sum);
            normHist.add(tmp);
        }

        return normHist;
    }

    private List<HrHistogramElement<Integer>> createHistogram(List<Integer> hrList, int barNumInHistogram) {
        if(hrList.size() < 1) return null;

        int minValue = Collections.min(hrList);
        int maxValue = Collections.max(hrList);
        int interval = (maxValue-minValue)/barNumInHistogram+1;
        if(interval < MIN_INTERVAL_IN_HISTOGRAM) interval = MIN_INTERVAL_IN_HISTOGRAM; // 直方图统计的心率最小间隔为10

        int[] histData = new int[barNumInHistogram];

        int num;
        for(int hr : hrList) {
            num = (hr-minValue)/interval;
            if(num >= barNumInHistogram) num = barNumInHistogram-1;
            histData[num]++;
        }

        List<HrHistogramElement<Integer>> histogram = new ArrayList<>();
        for(int i = 0; i < barNumInHistogram; i++) {
            int min = minValue + i*interval;
            HrHistogramElement<Integer> tmp = new HrHistogramElement<>(min, min+interval-1, histData[i]);
            histogram.add(tmp);
        }

        return histogram;
    }

    @Override
    public void process(int hr) {
        if(hr != INVALID_HR && isRecord) {
            hrList.add(hr);
        }
    }

    private List<Integer> hrFilter(int second) {
        List<Integer> hrFiltered = new ArrayList<>();
        double sum = 0.0;
        int num = 0;
        double period = 0.0;
        for(int hr : hrList) {
            sum += hr;
            num++;
            period += 60.0/hr;
            if(period >= second) {
                hrFiltered.add((int)sum/num);
                period -= second;
                sum = 0;
                num = 0;
            }
        }
        return hrFiltered;
    }

    public void setEcgHrInfoUpdatedListener(IEcgHrInfoUpdatedListener listener) {
        this.listener = listener;
    }

    public void removeEcgHrInfoUpdatedListener() {
        listener = null;
    }

}
