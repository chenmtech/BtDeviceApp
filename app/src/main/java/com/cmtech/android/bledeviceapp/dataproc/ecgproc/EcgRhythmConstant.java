package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import java.util.HashMap;
import java.util.Map;

public class EcgRhythmConstant {
    public static final int INVALID_LABEL = -1;
    public static final int NSR_LABEL = 0;
    public static final int NOISE_LABEL = 1;
    public static final int AFIB_LABEL = 2;
    public static final int OTHER_LABEL = 3;
    public static final int SB_LABEL = 4;
    public static final int ALL_ARRHYTHM_LABEL = 100;

    // 心律和它对应的描述字符串的映射
    public static final Map<Integer, String> RHYTHM_DESC_MAP = new HashMap<>(){{
        put(NSR_LABEL, "窦性心律");
        put(NOISE_LABEL, "噪声");
        put(AFIB_LABEL, "房颤");
        put(OTHER_LABEL, "其他异常");
        put(SB_LABEL, "窦性心动过缓");
    }};

    // 异常心律和它对应的描述字符串的映射
    public static final Map<Integer, String> ARRHYTHMIA_DESC_MAP = new HashMap<>(){{
        put(AFIB_LABEL, "房颤");
        put(OTHER_LABEL, "其他异常");
    }};
}
