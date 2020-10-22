package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.util.AttributeSet;

public class ScanEegView extends ScanWaveView {
    public static final int PIXEL_PER_GRID = 10; // 每个栅格包含的像素个数
    public static final float SECOND_PER_GRID = 0.04f; // 横向每个栅格代表的秒数，对应于走纸速度
    public static final float MV_PER_GRID = 0.01f; // 纵向每个栅格代表的mV，对应于灵敏度

    public ScanEegView(Context context) {
        super(context);
    }

    public ScanEegView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(int sampleRate, int value1mV, double zeroLocation) {
        int pixelPerData = Math.round(PIXEL_PER_GRID / (SECOND_PER_GRID * sampleRate)); // 计算横向分辨率
        float valuePerPixel = value1mV * MV_PER_GRID / PIXEL_PER_GRID; // 计算纵向分辨率
        setResolution(pixelPerData, valuePerPixel);
        setPixelPerGrid(PIXEL_PER_GRID);
        setZeroLocation(zeroLocation);
        reset();
    }
}
