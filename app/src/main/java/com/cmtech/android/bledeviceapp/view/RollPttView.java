package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.util.AttributeSet;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.record.BlePttRecord;

import java.util.Arrays;

public class RollPttView extends RollRecordView {
    private static final float SECOND_PER_GRID = 0.04f; // 横向每个栅格代表的秒数，对应于走纸速度
    private static final float MV_PER_GRID = 0.1f; // ECG视图的纵向每个栅格代表的mV
    private static final float PHYSIC_PER_GRID = 0.05f; // PPG视图的纵向每个栅格代表的物理量
    private static final int PIXEL_PER_GRID = 10; // 每个栅格包含的像素个数

    public RollPttView(Context context) {
        super(context);
    }

    public RollPttView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(BlePttRecord record, float[] zeroLocs) {
        super.setup(record, zeroLocs, SECOND_PER_GRID, new float[]{MV_PER_GRID,PHYSIC_PER_GRID}, PIXEL_PER_GRID);
    }

    public void setup(BlePttRecord record) {
        super.setup(record, SECOND_PER_GRID, new float[]{MV_PER_GRID,PHYSIC_PER_GRID}, PIXEL_PER_GRID);
    }
}
