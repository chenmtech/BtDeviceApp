package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

public class EcgRhythmDetectResultItem {
    private long startTime;
    private int label;

    public EcgRhythmDetectResultItem(long startTime, int label) {
        this.startTime = startTime;
        this.label = label;
    }

    public int getLabel() {
        return label;
    }

    public String toString() {
        return startTime+":"+label;
    }

    public long getStartTime() {
        return startTime;
    }
}
