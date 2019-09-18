package com.cmtech.android.bledeviceapp;

import java.io.File;

/**
 * BleDeviceConstant: 设备相关的常数
 * Created by bme on 2018/3/1.
 */

public class BleDeviceConstant {

    public static final String MY_BASE_UUID = "0a20XXXX-cce5-4025-a156-38ea833f6ef8"; // 基础UUID
    public static final String BT_BASE_UUID = "0000XXXX-0000-1000-8000-00805F9B34FB"; // 蓝牙标准UUID
    public static final String CCCUUID = "00002902-0000-1000-8000-00805f9b34fb"; // CCC UUID

    public final static int SCAN_TIMEOUT = -1; // 扫描超时时间，-1为一直扫描直到发现设备为止

    public final static int CONNECT_TIMEOUT = 60000; // 连接超时时间

    public static final String SCAN_DEVICE_NAME = "CM1.0"; // 扫描时过滤设备：只获取广播数据包中设备名为该名称的设备

    public static final File DIR_IMAGE = MyApplication.getContext().getExternalFilesDir("images"); // 图像文件DIR

    public static final File DIR_CACHE = MyApplication.getContext().getExternalCacheDir(); // 文件缓存cache目录


}
