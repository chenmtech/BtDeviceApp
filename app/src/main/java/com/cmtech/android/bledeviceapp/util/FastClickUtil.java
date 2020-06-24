package com.cmtech.android.bledeviceapp.util;

public class FastClickUtil {
    private static long lastClickTime;

    public synchronized static boolean isFastClick() {
        return isFastClick(500);
    }

    public synchronized static boolean isFastClick(int interval) {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < interval) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
