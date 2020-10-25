package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.util.AttributeSet;

import com.cmtech.android.bledevice.record.BleEegRecord10;

public class RollEegView extends RollRecordView {
    private static final int PIXEL_PER_GRID = 10; // 每个栅格包含的像素个数
    private static final float SECOND_PER_GRID = 0.04f; // 横向每个栅格代表的秒数，对应于走纸速度
    private static final float MV_PER_GRID = 0.01f; // 纵向每个栅格代表的mV，对应于灵敏度

    public RollEegView(Context context) {
        super(context);
    }

    public RollEegView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(BleEegRecord10 record, float zeroLocation) {
        super.setup(record, zeroLocation, SECOND_PER_GRID, MV_PER_GRID, PIXEL_PER_GRID);
    }
}
