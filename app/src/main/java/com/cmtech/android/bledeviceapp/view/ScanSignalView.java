package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.view
 * ClassName:      ScanSignalView
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/10/26 上午6:06
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/26 上午6:06
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class ScanSignalView extends ScanWaveView {
    public ScanSignalView(Context context) {
        super(context);
    }

    public ScanSignalView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置信号波形视图
     * @param zeroLocs 每个波形的零位置，它的长度代表波形个数
     * @param sampleRate 采样频率，每个波形数据的采样频率必须相同
     * @param gain 增益，即每个波形中的一个单位物理量对应的ADU值
     * @param secPerGrid 横向每个栅格对应的时间秒数
     * @param physicValuePerGrid 纵向每个栅格对应的物理量,比如：mv/grid
     * @param pixelPerGrid 每个栅格的像素个数，横向和纵向一样
     */
    public void setup(float[] zeroLocs, int sampleRate, int[] gain, float secPerGrid, float[] physicValuePerGrid, int pixelPerGrid) {
        // 计算横向分辨率
        int pixelPerData = Math.round(pixelPerGrid / (secPerGrid * sampleRate)); // 横向分辨率

        // 计算纵向分辨率
        float[] aduPerPixel = new float[gain.length];
        for(int i = 0; i < gain.length; i++)
            aduPerPixel[i] = gain[i] * physicValuePerGrid[i] / pixelPerGrid;

        setup(zeroLocs, aduPerPixel, pixelPerData, pixelPerGrid);
    }

    /**
     * 设置信号波形视图
     * @param waveNum 波形个数
     * @param sampleRate 采样频率，每个波形数据的采样频率必须相同
     * @param gain 增益，即每个波形中的一个单位物理量对应的ADU值
     * @param secPerGrid 横向每个栅格对应的时间秒数
     * @param physicValuePerGrid 纵向每个栅格对应的物理量,比如：mv/grid
     * @param pixelPerGrid 每个栅格的像素个数，横向和纵向一样
     */
    public void setup(int waveNum, int sampleRate, int[] gain, float secPerGrid, float[] physicValuePerGrid, int pixelPerGrid) {
        assert gain.length == waveNum;

        // 用波形个数计算零值位置
        float[] zeroLocs = new float[waveNum];
        for(int i = 0; i < waveNum; i++)
            zeroLocs[i] = (1.0f+2*i) / (2*waveNum);

        setup(zeroLocs, sampleRate, gain, secPerGrid, physicValuePerGrid, pixelPerGrid);
    }
}
