package com.cmtech.android.bledeviceapp.util;

import android.content.pm.PackageManager;

import com.cmtech.android.bledeviceapp.global.MyApplication;

/**
 * App版本Utils
 */

public class AppVersionUtils {
    // 获取版本号
    public static int getVersionCode() {
        int versionCode = 0;
        try {
            versionCode = MyApplication.getContext().getPackageManager().
                    getPackageInfo(MyApplication.getContext().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    // 获取版本名
    public static String getVerName() {
        String verName = "";
        try {
            verName = MyApplication.getContext().getPackageManager().
                    getPackageInfo(MyApplication.getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }
}