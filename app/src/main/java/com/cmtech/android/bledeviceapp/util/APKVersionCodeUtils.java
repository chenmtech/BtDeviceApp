package com.cmtech.android.bledeviceapp.util;

import android.content.pm.PackageManager;

import com.cmtech.android.bledeviceapp.global.MyApplication;

/**
 * APK版本Utils
 */

public class APKVersionCodeUtils {

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