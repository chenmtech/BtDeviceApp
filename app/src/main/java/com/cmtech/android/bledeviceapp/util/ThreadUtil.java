package com.cmtech.android.bledeviceapp.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * 线程类Util
 */
public class ThreadUtil {
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    public static void showToastInMainThread(Context context, int resId, int duration) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, resId, duration).show();
            }
        });
    }

    public static void showToastInMainThread(Context context, CharSequence text, int duration) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, duration).show();
            }
        });
    }

    public static void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }
}
