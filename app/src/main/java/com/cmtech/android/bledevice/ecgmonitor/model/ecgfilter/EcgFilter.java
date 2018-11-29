package com.cmtech.android.bledevice.ecgmonitor.model.ecgfilter;

import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.design.DCBlockDesigner;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.structure.StructType;

public class EcgFilter implements IEcgFilter {
    private IDigitalFilter dcBlock;
    private IDigitalFilter notch50Hz;

    public EcgFilter(int sampleRate) {
        // 准备0.5Hz基线漂移滤波器
        dcBlock = DCBlockDesigner.design(0.5, sampleRate);                   // 设计隔直滤波器
        dcBlock.createStructure(StructType.IIR_DCBLOCK);                            // 创建隔直滤波器专用结构

        // 准备50Hz陷波器
        notch50Hz = NotchDesigner.design(50, 0.5, sampleRate);           // 设计陷波器
        notch50Hz.createStructure(StructType.IIR_NOTCH);                            // 创建陷波器专用结构
    }

    @Override
    public double filter(double ecgSignal) {
        return notch50Hz.filter(dcBlock.filter(ecgSignal));
    }
}
