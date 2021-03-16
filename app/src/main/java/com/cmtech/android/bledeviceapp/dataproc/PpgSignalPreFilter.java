package com.cmtech.android.bledeviceapp.dataproc;

import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.design.DCBlockDesigner;
import com.cmtech.dsp.filter.design.FIRDesigner;
import com.cmtech.dsp.filter.design.FilterType;
import com.cmtech.dsp.filter.design.WinType;
import com.cmtech.dsp.filter.structure.StructType;

import static java.lang.Math.PI;


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

public class PpgSignalPreFilter implements ISignalFilter {
    private IDigitalFilter dcBlocker; // 隔直滤波器
    private IDigitalFilter lpFilter; // 低通FIR滤波器

    public PpgSignalPreFilter(int sampleRate) {
        design(sampleRate);
    }

    @Override
    public void design(int sampleRate) {
        // 准备0.5Hz基线漂移滤波器
        dcBlocker = DCBlockDesigner.design(0.5, sampleRate); // 设计隔直滤波器
        dcBlocker.createStructure(StructType.IIR_DCBLOCK); // 创建隔直滤波器专用结构

        // 准备15Hz低通滤波器
        int fp = 15;
        int fs = 20;
        double[] wp = {2*PI*fp/sampleRate};
        double[] ws = {2*PI*fs/sampleRate};
        lpFilter = FIRDesigner.design(wp, ws,1, 50, FilterType.LOWPASS, WinType.HAMMING);
    }

    @Override
    public double filter(double signal) {
        return lpFilter.filter(dcBlocker.filter(signal));
    }
}
