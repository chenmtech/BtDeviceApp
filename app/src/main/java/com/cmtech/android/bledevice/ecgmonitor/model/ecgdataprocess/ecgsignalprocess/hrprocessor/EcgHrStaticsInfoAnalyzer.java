package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess
 * ClassName:      EcgHrStaticsInfoAnalyzer
 * Description:    描述和分析Ecg心率统计信息
 * Author:         chenm
 * CreateDate:     2019/4/17 上午4:44
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/17 上午4:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class EcgHrStaticsInfoAnalyzer {
    private static final int HR_MIN_INTERVAL_IN_HISTOGRAM = 10;

    // 心率直方图的Element类
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

    private int secondInHrFilter;

    private List<Short> filteredHrList = new ArrayList<>();

    private short maxHr;

    private long sumHr;

    private double sumTmp = 0.0;

    private int numTmp = 0;

    private double periodTmp = 0.0;

    public EcgHrStaticsInfoAnalyzer(int secondInHrFilter) {
        this.secondInHrFilter = secondInHrFilter;
    }

    public EcgHrStaticsInfoAnalyzer(List<Short> hrList, int secondInHrFilter) {
        this(secondInHrFilter);

        for(Short hr : hrList)
            process(hr);
    }

    // 返回值：对hr滤波后，是否更新了filteredHrList
    public boolean process(short hr) {
        boolean updated = false;

        sumTmp += hr;

        numTmp++;

        periodTmp += 60.0/hr;

        if(periodTmp >= secondInHrFilter) {
            short average = (short)(sumTmp / numTmp);

            filteredHrList.add(average);

            if(maxHr < average) maxHr = average;

            sumHr += average;

            periodTmp -= secondInHrFilter;

            sumTmp = 0;

            numTmp = 0;

            updated = true;
        }
        return updated;
    }

    public void clear() {
        filteredHrList.clear();
        maxHr = 0;
        sumHr = 0;
        sumTmp = 0.0;
        numTmp = 0;
        periodTmp = 0.0;
    }

    public List<Short> getFilteredHrList() {
        return filteredHrList;
    }

    public List<HrHistogramElement<Float>> getNormHistogram(int barNumInHistogram) {
        return EcgHrStaticsInfoAnalyzer.createNormHistogram(filteredHrList, barNumInHistogram);
    }

    public short getMaxHr() {
        return maxHr;
    }

    public short getAverageHr() {
        if(filteredHrList.isEmpty()) return 0;
        else return (short) (sumHr/filteredHrList.size());
    }


    // 产生归一化直方图
    private static List<HrHistogramElement<Float>> createNormHistogram(List<Short> hrList, int barNum) {
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

    private static List<HrHistogramElement<Integer>> createHistogram(List<Short> hrList, int barNumInHistogram) {
        if(hrList.size() < 1) return null;

        short minValue = Collections.min(hrList);
        short maxValue = Collections.max(hrList);
        int interval = (maxValue-minValue)/barNumInHistogram+1;
        if(interval < HR_MIN_INTERVAL_IN_HISTOGRAM) interval = HR_MIN_INTERVAL_IN_HISTOGRAM; // 直方图统计的心率最小间隔为10

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
}
