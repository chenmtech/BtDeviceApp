package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

/**
 * 心电信号心律异常检测条目，表示一条检测结果
 */
public class EcgRhythmDetectItem {
    // 该条目的起始时间
    private final long startTime;

    // 该条目的心律异常标签值
    private final int label;

    public EcgRhythmDetectItem(long startTime, int label) {
        this.startTime = startTime;
        this.label = label;
    }

    public int getLabel() {
        return label;
    }

    public long getStartTime() {
        return startTime;
    }
}
