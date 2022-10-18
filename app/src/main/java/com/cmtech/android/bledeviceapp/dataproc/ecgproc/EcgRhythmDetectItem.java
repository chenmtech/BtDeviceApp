package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import java.util.HashMap;
import java.util.Map;

public class EcgRhythmDetectItem {
    public static final int NSR_LABEL = 0;
    public static final int NOISE_LABEL = 1;
    public static final int AF_LABEL = 2;
    public static final int OTHER_LABEL = 3;

    public static final Map<Integer, String> RESULT_TABLE = new HashMap<>(){{
        put(NSR_LABEL, "窦性心律");
        put(NOISE_LABEL, "噪声");
        put(AF_LABEL, "房颤");
        put(OTHER_LABEL, "其他异常");
    }};

    private long startTime;
    private int label;

    public EcgRhythmDetectItem(long startTime, int label) {
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

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
