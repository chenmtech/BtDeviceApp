package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.design.DCBlockDesigner;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.structure.StructType;


/**
  *
  * ClassName:      EcgPreFilter
  * Description:    Ecg Pre-Filter, including a baseline drift filter and a 50Hz notch filter
  * Author:         chenm
  * CreateDate:     2018-12-06 07:38
  * UpdateUser:     chenm
  * UpdateDate:     2019-07-03 07:38
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgPreFilter implements IEcgFilter {
    private static final double DEFAULT_BASELINE_CUTOFF_FREQ = 0.5; // default cut-off frequency of the baseline drift filter
    private static final int DEFAULT_NOTCH_FREQ = 50; // default notch central frequency
    private static final double DEFAULT_NOTCH_3DB_BANDWIDTH = 0.5; // default 3dB bandwidth of the notch filter

    private final double baselineFreq; // cut-off frequency of the baseline drift filter
    private final int notchFreq; // notch central frequency
    private IDigitalFilter dcBlocker; // a DC blocker filtering the baseline drift
    private IDigitalFilter notch; // a notch filter filtering the 50Hz noise

    public EcgPreFilter(int sampleRate) {
        this(sampleRate, DEFAULT_BASELINE_CUTOFF_FREQ, DEFAULT_NOTCH_FREQ);
    }

    public EcgPreFilter(int sampleRate, double baselineFreq, int notchFreq) {
        this.baselineFreq = baselineFreq;
        this.notchFreq = notchFreq;

        design(sampleRate);
    }

    @Override
    public void design(int sampleRate) {
        // 准备0.5Hz基线漂移滤波器
        dcBlocker = DCBlockDesigner.design(baselineFreq, sampleRate); // 设计隔直滤波器
        dcBlocker.createStructure(StructType.IIR_DCBLOCK); // 创建隔直滤波器专用结构

        // 准备50Hz陷波器
        notch = NotchDesigner.design(notchFreq, DEFAULT_NOTCH_3DB_BANDWIDTH, sampleRate);  // 设计陷波器
        notch.createStructure(StructType.IIR_NOTCH); // 创建陷波器专用结构
    }

    @Override
    public double filter(double ecgSignal) {
        return notch.filter(dcBlocker.filter(ecgSignal));
    }

}
