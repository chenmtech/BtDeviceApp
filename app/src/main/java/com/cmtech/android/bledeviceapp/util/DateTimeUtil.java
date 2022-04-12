package com.cmtech.android.bledeviceapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtil {
    public static final long INVALID_TIME = -1;

    public static String timeToString(long timeInMillis) {
        return new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒", Locale.CHINA).format(new Date(timeInMillis));
    }

    public static String timeToShortString(long timeInMillis) {
        return new SimpleDateFormat("yy-MM-dd HH:mm", Locale.CHINA).format(new Date(timeInMillis));
    }

    public static String timeToShortStringWithTodayYesterday(long timeInMillis) {
        return todayYesterday(timeInMillis) + new SimpleDateFormat(" HH:mm", Locale.CHINA).format(timeInMillis);
    }

    // a integer to xx:xx:xx
    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    // a integer to xx m xx s
    public static String secToMinute(int time) {
        int minute = 0;
        int second = 0;
        if(time > 0) {
            minute = time / 60;
            second = time % 60;
        }
        return unitFormat(minute)+"'"+unitFormat(second)+"''";
    }

    // a integer to xx时xx分xx秒
    public static String secToTimeInChinese(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00分00秒";
        else {
            minute = time / 60;
            if(minute == 0) {
                timeStr = unitFormat(time) + "秒";
            } else if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + "分" + unitFormat(second) + "秒";
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + "时" + unitFormat(minute) + "分" + unitFormat(second) + "秒";
            }
        }
        return timeStr;
    }

    private static String todayYesterday(long timeStamp) {
        long curTimeMillis = System.currentTimeMillis();
        Date curDate = new Date(curTimeMillis);
        int todayHoursSeconds = curDate.getHours() * 60 * 60;
        int todayMinutesSeconds = curDate.getMinutes() * 60;
        int todaySeconds = curDate.getSeconds();
        int todayMillis = (todayHoursSeconds + todayMinutesSeconds + todaySeconds) * 1000;
        long todayStartMillis = curTimeMillis - todayMillis;
        if(timeStamp >= todayStartMillis) {
            return "今天";
        }
        int oneDayMillis = 24 * 60 * 60 * 1000;
        long yesterdayStartMilis = todayStartMillis - oneDayMillis;
        if(timeStamp >= yesterdayStartMilis) {
            return "昨天";
        }
        long yesterdayBeforeStartMilis = yesterdayStartMilis - oneDayMillis;
        if(timeStamp >= yesterdayBeforeStartMilis) {
            return "前天";
        }
        return  new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(timeStamp);
    }

    private static String unitFormat(int i) {
        if (i >= 0 && i < 10)
            return  "0" + i;
        else
            return "" + i;
    }
}
