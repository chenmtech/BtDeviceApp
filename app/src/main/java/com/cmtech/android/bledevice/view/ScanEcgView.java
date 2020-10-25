package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.util.AttributeSet;

public class ScanEcgView extends ScanSignalView {
    public static final int PIXEL_PER_GRID = 10; // 每个小栅格包含的像素个数
    public static final float SECOND_PER_GRID = 0.04f; // 横向每个小栅格代表的秒数，对应于走纸速度
    public static final float MV_PER_GRID = 0.1f; // 纵向每个小栅格代表的mV，对应于灵敏度

    public ScanEcgView(Context context) {
        super(context);
    }

    public ScanEcgView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(int sampleRate, int caliValue, float zeroLocation) {
        super.setup(sampleRate, caliValue, zeroLocation, SECOND_PER_GRID, MV_PER_GRID, PIXEL_PER_GRID);
    }
}
