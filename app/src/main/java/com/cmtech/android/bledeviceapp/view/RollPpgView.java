package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.util.AttributeSet;

import com.cmtech.android.bledeviceapp.data.record.BlePpgRecord;

public class RollPpgView extends RollRecordView {
    private static final int PIXEL_PER_GRID = 10; // 每个栅格包含的像素个数
    private static final float SECOND_PER_GRID = 0.04f; // 横向每个栅格代表的秒数，对应于走纸速度
    private static final float MV_PER_GRID = 0.01f; // 纵向每个栅格代表的mV，对应于灵敏度

    public RollPpgView(Context context) {
        super(context);
    }

    public RollPpgView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(BlePpgRecord record, float zeroLocation) {
        super.setup(record, zeroLocation, SECOND_PER_GRID, MV_PER_GRID, PIXEL_PER_GRID);
    }
}
