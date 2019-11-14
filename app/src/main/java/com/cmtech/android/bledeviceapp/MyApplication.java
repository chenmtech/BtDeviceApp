package com.cmtech.android.bledeviceapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.cmtech.android.ble.BleConfig;
import com.cmtech.android.bledevice.ecgmonitor.device.EcgMonitorFactory;
import com.cmtech.android.bledevice.ecgmonitorweb.WebEcgMonitorFactory;
import com.cmtech.android.bledevice.siggenerator.model.SigGeneratorFactory;
import com.cmtech.android.bledevice.temphumid.model.TempHumidFactory;
import com.cmtech.android.bledevice.thermo.model.ThermoFactory;
import com.mob.MobSDK;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.BleDeviceConstant.AUTO_SCAN_INTERVAL;
import static com.cmtech.android.bledeviceapp.BleDeviceConstant.CONNECT_TIMEOUT;
import static com.cmtech.android.bledeviceapp.BleDeviceConstant.DATA_OPERATE_TIMEOUT;

/**
 * MyApplication
 * Created by bme on 2018/2/19.
 */

public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        // 初始化LitePal
        LitePal.initialize(getApplicationContext());
        LitePal.getDatabase();
        // BLE包配置
        BleConfig.getInstance().setConnectTimeout(CONNECT_TIMEOUT);
        BleConfig.getInstance().setDataOperateTimeout(DATA_OPERATE_TIMEOUT);
        BleConfig.getInstance().setAutoScanInterval(AUTO_SCAN_INTERVAL);
        // 配置支持的设备类型
        BleDeviceConfig.addSupportedDeviceType(EcgMonitorFactory.ECGMONITOR_DEVICE_TYPE);
        BleDeviceConfig.addSupportedDeviceType(TempHumidFactory.TEMPHUMID_DEVICE_TYPE);
        BleDeviceConfig.addSupportedDeviceType(ThermoFactory.THERMO_DEVICE_TYPE);
        BleDeviceConfig.addSupportedDeviceType(SigGeneratorFactory.SIGGENERATOR_DEVICE_TYPE);
        BleDeviceConfig.addSupportedDeviceType(WebEcgMonitorFactory.ECGWEBMONITOR_DEVICE_TYPE);
        // 初始化MobSDK
        MobSDK.init(getApplicationContext(), "2865551f849a2", "4e4d54b3cba5472505b5f251419ba502");
        // 初始化ViseLog
        ViseLog.getLogConfig()
                .configAllowLog(true)           //是否输出日志
                .configShowBorders(false)        //是否排版显示
                .configTagPrefix("BleDeviceApp")     //设置标签前缀
                .configLevel(Log.VERBOSE);      //设置日志最小输出级别，默认Log.VERBOSE
        ViseLog.plant(new LogcatTree());        //添加打印日志信息到Logcat的树
    }

    public static MyApplication getInstance() {
        return instance;
    }

    // 获取Application Context
    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static void showShortToastMessage(String msg) {
        Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showLongToastMessage(String msg) {
        Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
