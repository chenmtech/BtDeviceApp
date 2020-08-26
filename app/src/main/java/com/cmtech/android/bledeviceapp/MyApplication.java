package com.cmtech.android.bledeviceapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.cmtech.android.ble.core.BleDeviceCommonInfo;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.DeviceManager;
import com.cmtech.android.bledeviceapp.util.SystemTTS;
import com.mob.MobSDK;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;

import org.litepal.LitePal;

import java.util.List;

/**
 * MyApplication
 * Created by bme on 2018/2/19.
 */

public class MyApplication extends Application {
    private static MyApplication instance;
    private static SystemTTS tts; // text to speech
    private DeviceManager deviceManager;
    private AccountManager accountManager;

    private static int startedActivityCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // init LitePal
        LitePal.initialize(getApplicationContext());
        LitePal.getDatabase();

        // init MobSDK
        MobSDK.init(getApplicationContext(), "2865551f849a2", "4e4d54b3cba5472505b5f251419ba502");

        // init ViseLog
        ViseLog.getLogConfig()
                .configAllowLog(true)           //是否输出日志
                .configShowBorders(false)        //是否排版显示
                .configTagPrefix("BleDeviceApp")     //设置标签前缀
                .configLevel(Log.VERBOSE);      //设置日志最小输出级别，默认Log.VERBOSE
        ViseLog.plant(new LogcatTree());        //添加打印日志信息到Logcat的树

        // init text-to-speech instance
        tts = SystemTTS.getInstance(getApplicationContext());

        CrashHandler.getInstance().init(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                startedActivityCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                startedActivityCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        deviceManager = DeviceManager.getInstance();
        initDeviceManager();

        accountManager = AccountManager.getInstance();
    }

    public static MyApplication getInstance() {
        return instance;
    }

    // 获取Application Context
    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static DeviceManager getDeviceManager() {return instance.deviceManager;}

    public static AccountManager getAccountManager() {
        return instance.accountManager;
    }

    public static Account getAccount() {
        return instance.accountManager.getAccount();
    }

    public static void killProcess() {
        ViseLog.e("killProcess");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static SystemTTS getTTS() {
        return tts;
    }

    public static String getStr(int strId) {
        return instance.getString(strId);
    }

    public static boolean isRunInBackground() {
        return (startedActivityCount == 0);
    }

    // 初始化DeviceManager
    private void initDeviceManager() {
        List<BleDeviceCommonInfo> infos = LitePal.findAll(BleDeviceCommonInfo.class);
        for (DeviceCommonInfo info : infos) {
            deviceManager.createNewDevice(this, info);
        }
    }
}
