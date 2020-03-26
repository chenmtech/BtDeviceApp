package com.cmtech.android.bledeviceapp;

import android.os.Environment;

import com.cmtech.android.ble.utils.UuidUtil;

import java.io.File;
import java.util.UUID;

/**
 * AppConstant: App constant
 * Created by bme on 2018/3/1.
 */

public class AppConstant {
    public static final String MY_BASE_UUID = "0a20XXXX-cce5-4025-a156-38ea833f6ef8"; // my base UUID
    public static final String STANDARD_BLE_UUID = "0000XXXX-0000-1000-8000-00805F9B34FB"; // standard BLE UUID
    public static final UUID CCC_UUID = UuidUtil.stringToUUUID("2902", STANDARD_BLE_UUID); // client characteristic config UUID
    public static final int RECONNECT_INTERVAL = 6000; // reconnect interval, unit: millisecond
    public static final int SCAN_DURATION = 20000; // scan duration, unit: millisecond
    public static final File DIR_IMAGE = MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES); // image file directory
    public static final File DIR_CACHE = MyApplication.getContext().getExternalCacheDir(); // file cache directory
}
