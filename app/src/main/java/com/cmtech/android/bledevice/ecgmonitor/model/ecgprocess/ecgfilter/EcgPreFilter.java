package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgfilter;

import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.design.DCBlockDesigner;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.structure.StructType;

/**
 * EcgPreFilter: 心电信号预滤波，包含一个基线漂移滤除的隔直滤波器和一个工频干扰滤除的陷波器
 * Created by bme on 2018/12/06.
 */

public class EcgPreFilter implements IEcgFilter {
    protected final static double NOTCH_BANDWIDTH_3DB = 0.5;        // 陷波器的3dB带宽
    private final static double DEFAULT_BASELINEFREQ = 0.5;
    private final static int DEFAULT_POWERLINEFREQ = 50;

    private double baselineFreq = 0.5;          // 基线漂移截止频率
    private int powerlineFreq = 50;             // 工频

    private IDigitalFilter dcBlock;
    private IDigitalFilter notch50Hz;

    public EcgPreFilter(int sampleRate) {
        this(sampleRate, DEFAULT_BASELINEFREQ, DEFAULT_POWERLINEFREQ);
    }

    public EcgPreFilter(int sampleRate, double baselineFreq, int powerlineFreq) {
        this.baselineFreq = baselineFreq;
        this.powerlineFreq = powerlineFreq;

        // 准备0.5Hz基线漂移滤波器
        dcBlock = DCBlockDesigner.design(baselineFreq, sampleRate);                   // 设计隔直滤波器
        dcBlock.createStructure(StructType.IIR_DCBLOCK);                            // 创建隔直滤波器专用结构

        // 准备50Hz陷波器
        notch50Hz = NotchDesigner.design(powerlineFreq, NOTCH_BANDWIDTH_3DB, sampleRate);           // 设计陷波器
        notch50Hz.createStructure(StructType.IIR_NOTCH);                            // 创建陷波器专用结构
    }

    @Override
    public double filter(double ecgSignal) {
        return notch50Hz.filter(dcBlock.filter(ecgSignal));
    }

}
