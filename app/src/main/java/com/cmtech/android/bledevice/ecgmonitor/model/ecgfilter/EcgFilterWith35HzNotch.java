package com.cmtech.android.bledevice.ecgmonitor.model.ecgfilter;

import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.structure.StructType;

public class EcgFilterWith35HzNotch extends EcgFilter {
    private IDigitalFilter notch35Hz;

    public EcgFilterWith35HzNotch() {

    }

    @Override
    public void init(int sampleRate) {
        super.init(sampleRate);

        // 准备35Hz陷波器
        notch35Hz = NotchDesigner.design(35, 0.5, sampleRate);           // 设计陷波器
        notch35Hz.createStructure(StructType.IIR_NOTCH);                            // 创建陷波器专用结构
    }

    @Override
    public double filter(double ecgSignal) {
        return notch35Hz.filter(super.filter(ecgSignal));
    }
}
