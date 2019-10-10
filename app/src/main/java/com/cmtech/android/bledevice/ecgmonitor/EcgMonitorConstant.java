package com.cmtech.android.bledevice.ecgmonitor;

import android.os.Environment;

import com.cmtech.android.bledeviceapp.MyApplication;

import java.io.File;

/**
 * EcgMonitorConstant：心电监护仪常量
 * Created by bme on 2018/11/20.
 */

public class EcgMonitorConstant {
    public static final File ECG_FILE_DIR = MyApplication.getContext().getExternalFilesDir("ecgSignal"); // Ecg文件存放目录
    public static final String WECHAT_DOWNLOAD_DIR = Environment.getExternalStorageDirectory().getPath() + "/tencent/MicroMsg/Download"; // 微信的下载存储目录
    public static final boolean DEFAULT_WARN_WHEN_HR_ABNORMAL = true; // 缺省心率异常是否报警
    public static final int DEFAULT_HR_LOW_LIMIT = 50; // 缺省心率下限
    public static final int DEFAULT_HR_HIGH_LIMIT = 100; // 缺省心率上限
}
