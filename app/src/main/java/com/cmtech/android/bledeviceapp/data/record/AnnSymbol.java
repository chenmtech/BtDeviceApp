package com.cmtech.android.bledeviceapp.data.record;

import java.util.HashMap;
import java.util.Map;

/**
 * 信号注解符号
 */
public class AnnSymbol {
    public static final String INVALID_ANN_SYMBOL = "";
    public static final String ANN_LBBB = "L";
    public static final String ANN_RBBB = "R";

    public static final String ANN_NOISE = "~U";

    public static final String ANN_COMMENT = "\"";

    public static final String ANN_AB = "+(AB";
    public static final String ANN_AFIB = "+(AFIB";
    public static final String ANN_AFL = "+(AFL";
    public static final String ANN_VB = "+(B";
    public static final String ANN_BII = "+(BII";
    public static final String ANN_IVR = "+(IVR";
    public static final String ANN_NSR = "+(N";
    public static final String ANN_NOD = "+(NOD";
    public static final String ANN_PR = "+(P";
    public static final String ANN_PREX = "+(PREX";
    public static final String ANN_SB = "+(SBR";
    public static final String ANN_SVTA = "+(SVTA";
    public static final String ANN_T = "+(T";
    public static final String ANN_VFL = "+(VFL";
    public static final String ANN_VT = "+(VT";
    public static final String ANN_OTHER_ARRHYTHMIA = "+(OA";
    public static final String ANN_RHYTHM_ONSET = "+(";

    // 注解符号和它对应的描述字符串
    public static final Map<String, String> ANN_SYMBOL_DESCRIPTION_MAP = new HashMap<>(){{
        put(ANN_LBBB, "左束支阻滞心搏");
        put(ANN_RBBB, "右束支阻滞心搏");

        put(ANN_NOISE, "噪声");

        put(ANN_COMMENT, "评论");

        put(ANN_AB, "房性二联律");
        put(ANN_AFIB, "房颤");
        put(ANN_AFL, "房扑");
        put(ANN_VB, "室性二联律");
        put(ANN_BII, "二度心阻滞");
        put(ANN_IVR, "心室节律");
        put(ANN_NSR, "窦性心律");
        put(ANN_NOD, "房室结交界性心律");
        put(ANN_PR, "起搏心律");
        put(ANN_PREX, "预激综合征");
        put(ANN_SB, "窦性心动过缓");
        put(ANN_SVTA, "室上性快速心律失常");
        put(ANN_T, "室性三联律");
        put(ANN_VFL, "室扑");
        put(ANN_VT, "室性心动过速");
        put(ANN_OTHER_ARRHYTHMIA, "其他心律异常");
    }};
}
