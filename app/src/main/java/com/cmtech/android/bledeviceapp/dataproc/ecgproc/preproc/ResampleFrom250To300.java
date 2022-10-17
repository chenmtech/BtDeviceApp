package com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc;

import com.cmtech.dsp.filter.FIRFilter;
import com.cmtech.dsp.filter.design.FIRDesigner;
import com.cmtech.dsp.filter.design.FilterType;
import com.cmtech.dsp.filter.design.WinType;
import com.cmtech.dsp.filter.structure.StructType;
import com.cmtech.dsp.seq.RealSeq;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现信号重采样，即采样率的改变：从250Hz到300Hz
 */
public class ResampleFrom250To300 {
    //-------------------------------------------------常量
    // 输出采样率
    public static final int OUT_SAMPLE_RATE = 300;

    // 用来控制FIR低通滤波器的h(n)长度或阶次(见N)，可选择一个合适的
    private static final int K = 8;

    // 抽取率
    private static final int M = 5;

    // 插值率
    private static final int L = 6;

    // FIR低通滤波器的h(n)长度
    private static final int N = K*L*2-1;

    // FIR低通滤波器的群延时
    private static final int DELAY = (N-1)/2;

    // FIR低通滤波器
    private final FIRFilter filter;

    private int needSkipped = DELAY;

    private int hasSkipped = 0;

    public ResampleFrom250To300() {
        // FIR低通滤波器的截止频率
        double[] wc = {Math.PI/L};
        // 用海明窗设计
        WinType wType = WinType.HAMMING;
        // 低通
        FilterType fType = FilterType.LOWPASS;
        // 设计滤波器，得到h(n)
        RealSeq h = FIRDesigner.FIRUsingWindow(N, wc, wType, fType);
        // 创建滤波器
        filter = new FIRFilter(h);
        // 创建线性相位FIR滤波器结构
        filter.createStructure(StructType.FIR_LPF);
    }

    public List<Short> process(short ecgData) {
        short[] in = new short[L];
        in[0] = ecgData;

        List<Short> out = new ArrayList<>();
        for(short num : in) {
            short after = (short)(L*filter.filter(num));
            hasSkipped++;
            if(hasSkipped == needSkipped) {
                hasSkipped = 0;
                needSkipped = M;
                out.add(after);
            }
        }
        return out;
    }

    public void reset() {
        needSkipped = DELAY;
        hasSkipped = 0;
    }
}
