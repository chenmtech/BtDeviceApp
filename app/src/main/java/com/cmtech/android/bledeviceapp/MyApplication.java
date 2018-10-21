package com.cmtech.android.bledeviceapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.cmtech.android.ble.ViseBle;
import com.cmtech.android.ble.common.BleConfig;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;

import org.litepal.LitePal;

/**
 * Created by bme on 2018/2/19.
 */

public class MyApplication extends Application {
    private final static int SCAN_TIMEOUT = 12000;           // 扫描超时，不能太短，否则会导致扫描频繁scanning too frequently
    private final static int CONNECT_TIMEOUT = 18000;       // 连接超时

    // 上下文
    private static Context context;

    // ViseBle单件实例
    private static ViseBle viseBle;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        // 初始化ViseBle
        viseBle = ViseBle.getInstance();
        viseBle.init(this);
        BleConfig.getInstance().setScanTimeout(SCAN_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setConnectRetryCount(0).setOperateRetryCount(0);

        // 初始化LitePal
        LitePal.initialize(context);
        LitePal.getDatabase();

        // 初始化ViseLog
        ViseLog.getLogConfig()
                .configAllowLog(true)           //是否输出日志
                .configShowBorders(false)        //是否排版显示
                .configTagPrefix("BtDeviceApp")     //设置标签前缀
                //.configFormatTag("%d{HH:mm:ss:SSS} %t %c{-5}")//个性化设置标签，默认显示包名
                .configLevel(Log.VERBOSE);      //设置日志最小输出级别，默认Log.VERBOSE
        ViseLog.plant(new LogcatTree());        //添加打印日志信息到Logcat的树
    }

    public static Context getContext() {
        return context;
    }

    public static ViseBle getViseBle() {return viseBle;}
}
