package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.util.AttributeSet;

import com.cmtech.android.bledeviceapp.data.record.BleEegRecord;

import java.util.Arrays;

public class RollEegView extends RollRecordView {
    private static final float SECOND_PER_GRID = 0.04f; // 横向每个栅格代表的秒数，对应于走纸速度
    private static final float MV_PER_GRID = 0.01f; // 纵向每个栅格代表的mV，对应于灵敏度
    private static final int PIXEL_PER_GRID = 10; // 每个栅格包含的像素个数

    public RollEegView(Context context) {
        super(context);
    }

    public RollEegView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(BleEegRecord record, float[] zeroLocs) {
        float[] mvPerGrid = new float[record.getChannelNum()];
        Arrays.fill(mvPerGrid, MV_PER_GRID);
        super.setup(record, zeroLocs, SECOND_PER_GRID, mvPerGrid, PIXEL_PER_GRID);
    }

    public void setup(BleEegRecord record) {
        float[] mvPerGrid = new float[record.getChannelNum()];
        Arrays.fill(mvPerGrid, MV_PER_GRID);
        super.setup(record, SECOND_PER_GRID, mvPerGrid, PIXEL_PER_GRID);
    }
}
