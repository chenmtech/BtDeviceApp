package com.cmtech.android.bledeviceapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.cmtech.android.ble.BleConfig;
import com.cmtech.android.bledevice.ecg.device.EcgFactory;
import com.cmtech.android.bledevice.ecg.webecg.WebEcgFactory;
import com.cmtech.android.bledevice.hrmonitor.model.HRMonitorFactory;
import com.cmtech.android.bledevice.siggenerator.model.SigGeneratorFactory;
import com.cmtech.android.bledevice.temphumid.model.TempHumidFactory;
import com.cmtech.android.bledevice.thermo.model.ThermoFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;
import com.cmtech.android.bledeviceapp.util.SystemTTS;
import com.mob.MobSDK;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledeviceapp.AppConstant.RECONNECT_INTERVAL;

/**
 * MyApplication
 * Created by bme on 2018/2/19.
 */

public class MyApplication extends Application {
    private static MyApplication instance;
    private static SystemTTS tts; // text to speech

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // init LitePal
        LitePal.initialize(getApplicationContext());
        LitePal.getDatabase();

        // configure the BLE library
        BleConfig.setReconnInterval(RECONNECT_INTERVAL);

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
    }

    public static MyApplication getInstance() {
        return instance;
    }

    // 获取Application Context
    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static SystemTTS getTTS() {
        return tts;
    }

    public static void showMessageUsingShortToast(String msg) {
        Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showMessageUsingLongToast(String msg) {
        Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
