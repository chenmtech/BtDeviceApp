package com.cmtech.android.bledevice.ptt.model;

import com.cmtech.android.bledeviceapp.dataproc.ISignalFilter;
import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.design.DCBlockDesigner;
import com.cmtech.dsp.filter.design.FIRDesigner;
import com.cmtech.dsp.filter.design.FilterType;
import com.cmtech.dsp.filter.design.WinType;
import com.cmtech.dsp.filter.structure.StructType;
import com.vise.log.ViseLog;


/**
  *
  * ClassName:      SignalPreFilter
  * Description:    Signal Pre-Filter, including a baseline drift filter and a 50Hz notch filter
  * Author:         chenm
  * CreateDate:     2018-12-06 07:38
  * UpdateUser:     chenm
  * UpdateDate:     2019-07-03 07:38
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgSignalPreFilter implements ISignalFilter {
    private static final double DEFAULT_BASELINE_CUTOFF_FREQ = 0.5; // default cut-off frequency of the baseline drift filter

    private final double baselineFreq; // cut-off frequency of the baseline drift filter
    private IDigitalFilter dcBlocker; // a DC blocker filtering the baseline drift
    private IDigitalFilter lpFilter; // a lowpass filter filtering the noise which frequency is larger than 60Hz

    public EcgSignalPreFilter(int sampleRate) {
        this(sampleRate, DEFAULT_BASELINE_CUTOFF_FREQ);
    }

    public EcgSignalPreFilter(int sampleRate, double baselineFreq) {
        this.baselineFreq = baselineFreq;
        design(sampleRate);
    }

    @Override
    public void design(int sampleRate) {
        // 准备0.5Hz基线漂移滤波器
        dcBlocker = DCBlockDesigner.design(baselineFreq, sampleRate); // 设计隔直滤波器
        dcBlocker.createStructure(StructType.IIR_DCBLOCK); // 创建隔直滤波器专用结构

        // 准备60Hz低通滤波器
        int fp = 45;
        int fs = 50;
        lpFilter = FIRDesigner.design(new double[]{2 * Math.PI * fp / sampleRate}, new double[]{2 * Math.PI * fs / sampleRate},
                1, 50, FilterType.LOWPASS, WinType.HAMMING);
        ViseLog.e("ecgFilter size: " + lpFilter.getB().size());
    }

    @Override
    public double filter(double signal) {
        return lpFilter.filter(dcBlocker.filter(signal));
    }

}
