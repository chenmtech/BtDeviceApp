package com.cmtech.android.btdeviceapp;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by bme on 2018/2/19.
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(context);
    }

    public static Context getContext() {
        return context;
    }
}
