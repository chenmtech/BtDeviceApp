package com.cmtech.android.bledeviceapp.global;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.cmtech.android.ble.core.BleDeviceCommonInfo;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AppUpdateManager;
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
    private DeviceManager deviceManager;
    private AccountManager accountManager;
    private AppUpdateManager appUpdateManager;
    private SystemTTS tts;
    private int startedActivityCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // init LitePal
        LitePal.initialize(getApplicationContext());
        LitePal.getDatabase();

        // init MobSDK
        MobSDK.init(getApplicationContext());

        // init ViseLog
        ViseLog.getLogConfig()
                .configAllowLog(true)           //是否输出日志
                .configShowBorders(false)        //是否排版显示
                .configTagPrefix("BleDeviceApp")     //设置标签前缀
                .configLevel(Log.VERBOSE);      //设置日志最小输出级别，默认Log.VERBOSE
        ViseLog.plant(new LogcatTree());        //添加打印日志信息到Logcat的树

        new CrashHandler(this);

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

        deviceManager = new DeviceManager();
        initDeviceManager();

        accountManager = new AccountManager();

        appUpdateManager = new AppUpdateManager();

        tts = new SystemTTS(this);
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static DeviceManager getDeviceManager() {return instance.deviceManager;}

    public static AccountManager getAccountManager() {
        return instance.accountManager;
    }

    public static AppUpdateManager getAppUpdateManager() {
        return instance.appUpdateManager;
    }

    public static SystemTTS getTts() {
        return instance.tts;
    }

    public static Account getAccount() {
        return instance.accountManager.getAccount();
    }

    public static int getAccountId() {
        Account account = instance.accountManager.getAccount();
        if(account == null) return INVALID_ID;
        else return account.getAccountId();
    }

    public static void killProcess() {
        ViseLog.e("killProcess");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static String getStr(int strId) {
        return instance.getString(strId);
    }

    public static boolean isRunInBackground() {
        return (instance.startedActivityCount == 0);
    }


    // 初始化DeviceManager
    private void initDeviceManager() {
        List<BleDeviceCommonInfo> infos = LitePal.findAll(BleDeviceCommonInfo.class);
        for (DeviceCommonInfo info : infos) {
            deviceManager.createNewDevice(this, info);
        }
    }

}
