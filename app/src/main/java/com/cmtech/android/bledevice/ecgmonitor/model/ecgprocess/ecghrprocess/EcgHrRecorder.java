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
        void onUpdateEcgHrInfo(List<Short> filteredHrList, List<HrHistogramElement<Float>> normHistogram, short maxHr, short averageHr);
    }

    private IEcgHrInfoUpdatedListener listener;

    private List<Short> hrList;

    private boolean isRecord = true;

    // 心率直方图的一个单元类
    public static class HrHistogramElement<T> {
        private short minValue;
        private short maxValue;
        private T histValue;

        HrHistogramElement(short minValue, short maxValue, T histValue) {
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

        short getMinValue() {
            return minValue;
        }

        short getMaxValue() {
            return maxValue;
        }
    }

    public EcgHrRecorder(IEcgHrInfoUpdatedListener listener) {
        this(null, listener);
    }

    public EcgHrRecorder(List<Short> hrList, IEcgHrInfoUpdatedListener listener) {
        this.listener = listener;
        if(hrList != null)
            this.hrList = hrList;
        else
            this.hrList = new ArrayList<>();
    }

    public List<Short> getHrList() {
        return hrList;
    }

    public void setHrList(List<Short> hrList) {
        this.hrList = hrList;
    }

    public void setRecord(boolean record) {
        isRecord = record;
    }

    // 更新心率信息
    public void updateHrInfo(int secondInHrFilter, int barNumInHistogram) {
        List<Short> hrFiltered = hrFilter(secondInHrFilter);

        List<HrHistogramElement<Float>> normHistogram = createNormHistogram(hrFiltered, barNumInHistogram);

        short maxHr = 0;
        short averageHr = 0;
        if(!hrFiltered.isEmpty()) {
            maxHr = Collections.max(hrFiltered);

            int hrSum = 0;
            for (int hr : hrFiltered) {
                hrSum += hr;
            }
            averageHr = (short) (hrSum / hrFiltered.size());
        }

        if(listener != null)
            listener.onUpdateEcgHrInfo(hrFiltered, normHistogram, maxHr, averageHr);
    }

    // 重置心率数据
    public synchronized void reset() {
        hrList.clear();
        if(listener != null)
            listener.onUpdateEcgHrInfo(null, null, (short) 0, (short) 0);
    }


    // 产生归一化直方图
    private List<HrHistogramElement<Float>> createNormHistogram(List<Short> hrList, int barNum) {
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

    private List<HrHistogramElement<Integer>> createHistogram(List<Short> hrList, int barNumInHistogram) {
        if(hrList.size() < 1) return null;

        short minValue = Collections.min(hrList);
        short maxValue = Collections.max(hrList);
        int interval = (maxValue-minValue)/barNumInHistogram+1;
        if(interval < MIN_INTERVAL_IN_HISTOGRAM) interval = MIN_INTERVAL_IN_HISTOGRAM; // 直方图统计的心率最小间隔为10

        int[] histData = new int[barNumInHistogram];

        int num;
        for(short hr : hrList) {
            num = (hr-minValue)/interval;
            if(num >= barNumInHistogram) num = barNumInHistogram-1;
            histData[num]++;
        }

        List<HrHistogramElement<Integer>> histogram = new ArrayList<>();
        for(int i = 0; i < barNumInHistogram; i++) {
            short min = (short) (minValue + i*interval);
            HrHistogramElement<Integer> tmp = new HrHistogramElement<>(min, (short) (min+interval-1), histData[i]);
            histogram.add(tmp);
        }

        return histogram;
    }

    @Override
    public synchronized void process(short hr) {
        if(hr != INVALID_HR && isRecord) {
            hrList.add(hr);
        }
    }

    public void close() {
        isRecord = false;
        listener = null;
    }

    private List<Short> hrFilter(int second) {
        List<Short> hrFiltered = new ArrayList<>();
        double sum = 0.0;
        int num = 0;
        double period = 0.0;
        for(short hr : hrList) {
            sum += hr;
            num++;
            period += 60.0/hr;
            if(period >= second) {
                hrFiltered.add((short)(sum/num));
                period -= second;
                sum = 0;
                num = 0;
            }
        }
        return hrFiltered;
    }

}
