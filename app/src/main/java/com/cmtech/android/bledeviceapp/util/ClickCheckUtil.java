package com.cmtech.android.bledeviceapp.util;

/**
 * 用于判断是否是两次快速点击
 */
public class ClickCheckUtil {
    // 用于判断快速点击的时间间隔，ms
    private static final int DEFAULT_INTERVAL = 500;

    // 上次点击的时刻, ms
    private static long lastClickTime;

    public synchronized static boolean isFastClick() {
        return isFastClick(DEFAULT_INTERVAL);
    }

    /**
     * 是否为快速点击
     * @param interval 快速点击的时间间隔
     * @return true-是快速点击
     */
    public synchronized static boolean isFastClick(int interval) {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < interval) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
