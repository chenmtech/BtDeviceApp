package com.cmtech.android.bledevicecore.model;

import com.cmtech.android.bledeviceapp.MyApplication;

import java.io.File;

public class BleDeviceConstant {
    // 基础UUID
    public static final String MY_BASE_UUID = "0a20XXXX-cce5-4025-a156-38ea833f6ef8";
    // 蓝牙标准UUID
    public static final String BT_BASE_UUID = "0000XXXX-0000-1000-8000-00805F9B34FB";
    // CCC UUID
    public static final String CCCUUID = "00002902-0000-1000-8000-00805f9b34fb";

    // 重新连接时间间隔，注意：反复多次重连会导致系统禁用蓝牙
    public final static int RECONNECT_INTERVAL = 6000;
    // 扫描超时
    public final static int SCAN_TIMEOUT = 12000;
    // 连接超时
    public final static int CONNECT_TIMEOUT = 25000;
    // 连接重试次数
    public final static int CONNECT_RETRY_COUNT = 0;
    // 数据操作重试次数
    public final static int OPDATA_RETRY_COUNT = 0;
    // 扫描时只获取广播数据包中设备名为指定名称的设备
    public static final String SCAN_DEVICE_NAME = "CM1.0";
    // 图像文件DIR
    public static final File IMAGEDIR = MyApplication.getContext().getExternalFilesDir("images");
    // 文件缓存目录
    public static final File CACHEDIR = MyApplication.getContext().getExternalCacheDir();
}
