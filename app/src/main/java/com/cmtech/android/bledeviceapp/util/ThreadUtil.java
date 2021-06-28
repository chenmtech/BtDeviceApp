package com.cmtech.android.bledeviceapp.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.util
 * ClassName:      ThreadUtil
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2021/6/29 上午5:16
 * UpdateUser:     更新者
 * UpdateDate:     2021/6/29 上午5:16
 * UpdateRemark:   更新说明
 * Version:        1.0
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
