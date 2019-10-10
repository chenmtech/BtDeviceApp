package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.filter;

import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.structure.StructType;

/**
 *
 * ClassName:      EcgPreFilterWith35HzNotch
 * Description:    心电信号预滤波器，包含一个基线漂移滤除的隔直滤波器和一个工频干扰滤除的陷波器
 * Author:         chenm
 * CreateDate:     2018-12-06 07:38
 * UpdateUser:     chenm
 * UpdateDate:     2019-07-03 07:38
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class EcgPreFilterWith35HzNotch extends EcgPreFilter {
    private IDigitalFilter notch35Hz;

    public EcgPreFilterWith35HzNotch(int sampleRate) {
        super(sampleRate);

        // 准备35Hz陷波器
        notch35Hz = NotchDesigner.design(35, NOTCH_BANDWIDTH_3DB, sampleRate); // 设计陷波器
        notch35Hz.createStructure(StructType.IIR_NOTCH); // 创建陷波器专用结构
    }

    @Override
    public void updateSampleRate(int sampleRate) {
        super.updateSampleRate(sampleRate);

        // 准备35Hz陷波器
        notch35Hz = NotchDesigner.design(35, NOTCH_BANDWIDTH_3DB, sampleRate); // 设计陷波器
        notch35Hz.createStructure(StructType.IIR_NOTCH); // 创建陷波器专用结构
    }

    @Override
    public double filter(double ecgSignal) {
        return notch35Hz.filter(super.filter(ecgSignal));
    }
}
