package com.cmtech.android.bledevice.ecgmonitor;

import com.cmtech.android.bledeviceapp.MyApplication;

import java.io.File;

/**
 * EcgMonitorConstant：心电监护仪常量
 * Created by bme on 2018/11/20.
 */

public class EcgMonitorConstant {
    // Ecg文件存放目录
    public static final File ECGFILEDIR = MyApplication.getContext().getExternalFilesDir("ecgSignal");

    // 文件缓存目录
    public static final File CACHEDIR = MyApplication.getContext().getExternalCacheDir();

    // Ecg文件数据操作时分块的大小,单位字节
    public static final int ECG_BLOCK_LEN = 512;        // 添加和删除评论时，每次移动的文件数据块的大小
}
