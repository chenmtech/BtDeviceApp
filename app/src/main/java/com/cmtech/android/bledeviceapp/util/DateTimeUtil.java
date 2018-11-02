package com.cmtech.android.bledeviceapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
    public static String timeToStringWithSimpleFormat(long timeInMillis) {
        return new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒").format(new Date(timeInMillis));
    }

    public static String timeToStringWithShortFormat(long timeInMillis) {
        return new SimpleDateFormat("yy-MM-dd HH:mm").format(new Date(timeInMillis));
    }
}
