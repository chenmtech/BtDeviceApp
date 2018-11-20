package com.cmtech.android.bledevice.ecgmonitor;

import com.cmtech.android.bledeviceapp.MyApplication;

import java.io.File;

/**
 * 心电监护仪常量
 * Created by bme on 2018/11/20.
 */

public class EcgMonitorConstant {
    // Ecg文件存放目录
    public static final File ECGFILEDIR = MyApplication.getContext().getExternalFilesDir("ecgSignal");
}
