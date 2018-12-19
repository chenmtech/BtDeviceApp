package com.cmtech.android.bledeviceapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.cmtech.android.ble.ViseBle;
import com.cmtech.android.bledevicecore.BleDeviceConfig;
import com.cmtech.android.bledevicecore.BleDeviceConstant;
import com.mob.MobSDK;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;

import org.litepal.LitePal;

import static com.cmtech.android.bledevicecore.BleDeviceConstant.CONNECT_RETRY_COUNT;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.CONNECT_TIMEOUT;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.OPDATA_RETRY_COUNT;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.RECONNECT_INTERVAL;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.SCAN_DEVICE_NAME;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.SCAN_TIMEOUT;

/**
 * MyApplication
 * Created by bme on 2018/2/19.
 */

public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    // ViseBle单件实例
    private ViseBle viseBle = ViseBle.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // 先进行配置
        BleDeviceConfig.setScanTimeout(SCAN_TIMEOUT);
        BleDeviceConfig.setConnectTimeout(CONNECT_TIMEOUT);
        BleDeviceConfig.setReconnectInterval(RECONNECT_INTERVAL);
        BleDeviceConfig.setConnectRetryCount(CONNECT_RETRY_COUNT);
        BleDeviceConfig.setOpDataRetryCount(OPDATA_RETRY_COUNT);


        Context context = getApplicationContext();
        // 初始化ViseBle
        viseBle.init(context);

        // 初始化LitePal
        LitePal.initialize(context);
        LitePal.getDatabase();

        // 初始化ShareSDK
        MobSDK.init(context, "2865551f849a2", "4e4d54b3cba5472505b5f251419ba502");

        // 初始化ViseLog
        ViseLog.getLogConfig()
                .configAllowLog(true)           //是否输出日志
                .configShowBorders(false)        //是否排版显示
                .configTagPrefix("BleDeviceApp")     //设置标签前缀
                //.configFormatTag("%d{HH:mm:ss:SSS} %t %c{-5}")//个性化设置标签，默认显示包名
                .configLevel(Log.VERBOSE);      //设置日志最小输出级别，默认Log.VERBOSE
        ViseLog.plant(new LogcatTree());        //添加打印日志信息到Logcat的树
    }

    // 获取Application Context
    public static Context getContext() {
        return instance.getApplicationContext();
    }
}
