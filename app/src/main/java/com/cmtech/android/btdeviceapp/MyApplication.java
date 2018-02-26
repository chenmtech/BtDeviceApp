package com.cmtech.android.btdeviceapp;

import android.app.Application;
import android.content.Context;

import com.cmtech.android.ble.ViseBle;

import org.litepal.LitePal;

/**
 * Created by bme on 2018/2/19.
 */

public class MyApplication extends Application {
    private static Context context;
    private static ViseBle viseBle;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        viseBle = ViseBle.getInstance();
        viseBle.init(this);
        LitePal.initialize(context);
        LitePal.getDatabase();
    }

    public static Context getContext() {
        return context;
    }

    public static ViseBle getViseBle() {return viseBle;}
}
