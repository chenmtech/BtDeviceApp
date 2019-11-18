package com.cmtech.android.bledeviceapp;

import android.os.Environment;

import java.io.File;

/**
 * AppConstant: App常数
 * Created by bme on 2018/3/1.
 */

public class AppConstant {
    public static final String MY_BASE_UUID = "0a20XXXX-cce5-4025-a156-38ea833f6ef8"; // 我的基础UUID
    public static final int CONNECT_TIMEOUT = 60000; // 连接超时时间
    public static final int DATA_OPERATE_TIMEOUT = 3000; // 数据操作超时时间
    public static final int AUTO_SCAN_INTERVAL = 10; // 自动扫描间隔，秒
    public static final String SCAN_DEVICE_NAME = "CM1.0"; // 扫描时过滤的设备名称：只获取广播数据包中设备名为该名称的设备
    public static final File DIR_IMAGE = MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES); // 图像文件DIR
    public static final File DIR_CACHE = MyApplication.getContext().getExternalCacheDir(); // 文件缓存cache目录
}
