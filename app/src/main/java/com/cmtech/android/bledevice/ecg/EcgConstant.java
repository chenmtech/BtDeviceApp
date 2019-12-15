package com.cmtech.android.bledevice.ecg;

import android.os.Environment;

import com.cmtech.android.bledeviceapp.MyApplication;

import java.io.File;

/**
 * EcgConstant：心电监护仪常量
 * Created by bme on 2018/11/20.
 */

public class EcgConstant {
    private static final String DIRECTORY_ECG_SIGNALS = "ecgSignal";
    public static final File DIR_ECG_SIGNAL = MyApplication.getContext().getExternalFilesDir(DIRECTORY_ECG_SIGNALS); // Ecg文件存放目录
    public static final File DIR_WECHAT_DOWNLOAD = new File(Environment.getExternalStorageDirectory().getPath() + "/tencent/MicroMsg/Download");//MyApplication.getContext().getExternalFilesDir("/tencent/MicroMsg/Download"); // 微信的下载存储目录
    public static final boolean DEFAULT_WARN_WHEN_HR_ABNORMAL = true; // 缺省心率异常是否报警
    public static final int DEFAULT_HR_LOW_LIMIT = 50; // 缺省心率下限
    public static final int DEFAULT_HR_HIGH_LIMIT = 120; // 缺省心率上限
}
