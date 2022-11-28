package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import java.util.HashMap;
import java.util.Map;

public class EcgAnnotationConstant {
    public static final String INVALID_ANN_SYMBOL = "";
    public static final String ANN_NSR_SYMBOL = "+(N";
    public static final String ANN_NOISE_SYMBOL = "~";
    public static final String ANN_AFIB_SYMBOL = "+(AFIB";
    public static final String ANN_SB_SYMBOL = "+(SBR";
    public static final String ANN_OTHER_ARRHYTHMIA_SYMBOL = "other_arr";

    // 心律和它对应的描述字符串的映射
    public static final Map<String, String> ANNOTATION_DESCRIPTION_MAP = new HashMap<>(){{
        put(ANN_NSR_SYMBOL, "窦性心律");
        put(ANN_NOISE_SYMBOL, "噪声");
        put(ANN_AFIB_SYMBOL, "房颤");
        put(ANN_SB_SYMBOL, "窦性心动过缓");
        put(ANN_OTHER_ARRHYTHMIA_SYMBOL, "其他心律异常");
    }};

    // 异常心律和它对应的描述字符串的映射
/*    public static final Map<Integer, String> ARRHYTHMIA_DESC_MAP = new HashMap<>(){{
        put(AFIB_LABEL, "房颤");
        put(OTHER_LABEL, "其他异常");
    }};*/
}
