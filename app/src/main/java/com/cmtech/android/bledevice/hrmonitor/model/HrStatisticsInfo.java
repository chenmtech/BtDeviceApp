package com.cmtech.android.bledevice.hrmonitor.model;

import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;


/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess
 * ClassName:      HrStatisticsInfo
 * Description:    heart rate statistics information
 * Author:         chenm
 * CreateDate:     2019/4/17 上午4:44
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/17 上午4:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HrStatisticsInfo {
    // 心率直方图的Element类
    public static class HrHistogramElement<T> {
        private final short minValue;
        private final short maxValue;
        private final String barTitle; // histogram bar title string
        private T histValue; // 直方图值

        HrHistogramElement(short minValue, short maxValue, T histValue, String barTitle) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.histValue = histValue;
            this.barTitle = barTitle;
        }

        public String getBarTitle() {
            return barTitle;
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

    private final int filterWidth; // hr filter width, unit: second
    private short filteredHr = 0; // moving-average filtered hr value
    private final List<HrHistogramElement<Integer>> hrHistogram = new ArrayList<>();
    private short hrMax;
    private long hrSum;
    private long hrNum;
    private long preTime = 0;

    private int sumTmp = 0;
    private int numTmp = 0;
    private int periodTmp = 0;

    public HrStatisticsInfo(int filterWidth) {
        this.filterWidth = filterWidth;
        hrHistogram.add(new HrHistogramElement<Integer>((short)0, (short)121, 0, "平静心率"));
        hrHistogram.add(new HrHistogramElement<Integer>((short)122, (short)131, 0, "热身放松"));
        hrHistogram.add(new HrHistogramElement<Integer>((short)132, (short)141, 0, "有氧燃脂"));
        hrHistogram.add(new HrHistogramElement<Integer>((short)142, (short)152, 0, "有氧耐力"));
        hrHistogram.add(new HrHistogramElement<Integer>((short)153, (short)162, 0, "无氧耐力"));
        hrHistogram.add(new HrHistogramElement<Integer>((short)163, (short)1000, 0, "极限冲刺"));
    }

    /**
     * process hr value
     * @param hr heart rate value
     * @return is the hrlist updated after processing hr value
     */
    public boolean process(short hr, long time) {
        hrSum += hr;
        hrNum++;
        if(hrMax < hr) hrMax = hr;

        long tmp = Math.round((time-preTime)/1000.0); // ms to second
        int interval = (tmp > filterWidth) ? filterWidth : (int)tmp;
        preTime = time;
        for(HrHistogramElement<Integer> ele : hrHistogram) {
            if(hr < ele.maxValue) {
                ele.histValue += interval;
                break;
            }
        }

        boolean updated = false;
        sumTmp += hr;
        numTmp++;
        periodTmp += interval;
        if(periodTmp >= filterWidth) {
            filteredHr = (short)(sumTmp / numTmp);
            periodTmp -= filterWidth;
            sumTmp = 0;
            numTmp = 0;
            updated = true;
        }
        ViseLog.e("" + interval + " " + periodTmp + " " + updated);
        return updated;
    }

    public void clear() {
        filteredHr = 0;
        for(HrHistogramElement<Integer> ele : hrHistogram)
            ele.histValue = 0;
        hrMax = 0;
        hrSum = 0;
        hrNum = 0;
        preTime = 0;
        sumTmp = 0;
        numTmp = 0;
        periodTmp = 0;
    }

    public short getFilteredHr() {
        return filteredHr;
    }

    public short getHrMax() {
        return hrMax;
    }

    public short getHrAve() {
        return (hrNum == 0) ? 0 : (short) (hrSum / hrNum);
    }

    public List<HrHistogramElement<Integer>> getHrHistogram() {
        return hrHistogram;
    }
}
