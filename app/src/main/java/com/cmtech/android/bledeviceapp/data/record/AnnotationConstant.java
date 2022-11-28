package com.cmtech.android.bledeviceapp.data.record;

import java.util.HashMap;
import java.util.Map;

public class AnnotationConstant {
    public static final String INVALID_ANN_SYMBOL = "";
    public static final String ANN_LBBB_SYMBOL = "L";
    public static final String ANN_RBBB_SYMBOL = "R";

    public static final String ANN_NOISE_SYMBOL = "~U";

    public static final String ANN_AB_SYMBOL = "+(AB";
    public static final String ANN_AFIB_SYMBOL = "+(AFIB";
    public static final String ANN_AFL_SYMBOL = "+(AFL";
    public static final String ANN_VB_SYMBOL = "+(B";
    public static final String ANN_BII_SYMBOL = "+(BII";
    public static final String ANN_IVR_SYMBOL = "+(IVR";
    public static final String ANN_NSR_SYMBOL = "+(N";
    public static final String ANN_NOD_SYMBOL = "+(NOD";
    public static final String ANN_PR_SYMBOL = "+(P";
    public static final String ANN_PREX_SYMBOL = "+(PREX";
    public static final String ANN_SB_SYMBOL = "+(SBR";
    public static final String ANN_SVTA_SYMBOL = "+(SVTA";
    public static final String ANN_T_SYMBOL = "+(T";
    public static final String ANN_VFL_SYMBOL = "+(VFL";
    public static final String ANN_VT_SYMBOL = "+(VT";
    public static final String ANN_OTHER_ARRHYTHMIA_SYMBOL = "+(OA";

    // 注解符号和它对应的描述字符串
    public static final Map<String, String> ANNOTATION_DESCRIPTION_MAP = new HashMap<>(){{
        put(ANN_LBBB_SYMBOL, "左束支阻滞");
        put(ANN_RBBB_SYMBOL, "右束支阻滞");

        put(ANN_NOISE_SYMBOL, "噪声");

        put(ANN_AB_SYMBOL, "房性二联律");
        put(ANN_NSR_SYMBOL, "窦性心律");
        put(ANN_AFL_SYMBOL, "房扑");
        put(ANN_AFIB_SYMBOL, "房颤");
        put(ANN_SB_SYMBOL, "窦性心动过缓");
        put(ANN_SVTA_SYMBOL, "室上性心动过速");
        put(ANN_OTHER_ARRHYTHMIA_SYMBOL, "其他心律异常");
    }};
}
