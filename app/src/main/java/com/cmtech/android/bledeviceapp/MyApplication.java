package com.cmtech.android.bledeviceapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.cmtech.android.bledevicecore.model.BleDeviceConfig;
import com.cmtech.android.bledevicecore.model.BleDeviceConstant;
import com.mob.MobSDK;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;

import org.litepal.LitePal;

import cn.sharesdk.framework.ShareSDK;

import static com.cmtech.android.bledevicecore.model.BleDeviceConstant.CONNECT_TIMEOUT;
import static com.cmtech.android.bledevicecore.model.BleDeviceConstant.RECONNECT_INTERVAL;
import static com.cmtech.android.bledevicecore.model.BleDeviceConstant.SCAN_DEVICE_NAME;
import static com.cmtech.android.bledevicecore.model.BleDeviceConstant.SCAN_TIMEOUT;

/**
 * Created by bme on 2018/2/19.
 */

public class MyApplication extends Application {
    // 上下文
    private static Context context;

    private static BleDeviceConfig deviceConfig;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        // 初始化BleDeviceConfig
        deviceConfig = BleDeviceConfig.getInstance();
        deviceConfig.setBaseUuid(BleDeviceConstant.MY_BASE_UUID);
        deviceConfig.setScanTimeout(SCAN_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setReconnectInterval(RECONNECT_INTERVAL);
        deviceConfig.setScanDeviceName(SCAN_DEVICE_NAME);

        // 初始化LitePal
        LitePal.initialize(context);
        LitePal.getDatabase();

        // 初始化ShareSDK
        MobSDK.init(context, "2865551f849a2", "4e4d54b3cba5472505b5f251419ba502");

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
}
