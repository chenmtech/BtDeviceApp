package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.ecgfilter;

import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.structure.StructType;

public class EcgPreFilterWith35HzNotch extends EcgPreFilter {
    private IDigitalFilter notch35Hz;

    public EcgPreFilterWith35HzNotch(int sampleRate) {
        super(sampleRate);

        // 准备35Hz陷波器
        notch35Hz = NotchDesigner.design(35, NOTCH_BANDWIDTH_3DB, sampleRate);           // 设计陷波器
        notch35Hz.createStructure(StructType.IIR_NOTCH);                            // 创建陷波器专用结构
    }

    @Override
    public double filter(double ecgSignal) {
        return notch35Hz.filter(super.filter(ecgSignal));
    }
}
