package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import java.util.HashMap;
import java.util.Map;

public class EcgRhythmConstant {
    public static final int INVALID_LABEL = -1;
    public static final int NSR_LABEL = 0;
    public static final int NOISE_LABEL = 1;
    public static final int AF_LABEL = 2;
    public static final int OTHER_LABEL = 3;
    public static final int ALL_ARRHYTHM_LABEL = 100;

    // 所有心律标签
    public static final Map<Integer, String> RHYTHM_LABEL_MAP = new HashMap<>(){{
        put(NSR_LABEL, "窦性心律");
        put(NOISE_LABEL, "噪声");
        put(AF_LABEL, "房颤");
        put(OTHER_LABEL, "其他异常");
    }};

    // 异常标签
    public static final Map<Integer, String> ARRHYTHM_LABEL_MAP = new HashMap<>(){{
        put(AF_LABEL, "房颤");
        put(OTHER_LABEL, "其他异常");
    }};
}
