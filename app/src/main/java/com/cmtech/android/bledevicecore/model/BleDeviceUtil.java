package com.cmtech.android.bledevicecore.model;

import android.app.Activity;
import android.content.Context;

import com.cmtech.android.ble.utils.BleUtil;

public class BleDeviceUtil {
    // 基础UUID
    public static final String MY_BASE_UUID = "0a20XXXX-cce5-4025-a156-38ea833f6ef8";

    //
    public static final String BT_BASE_UUID = "0000XXXX-0000-1000-8000-00805F9B34FB";

    // CCC UUID
    public static final String CCCUUID = "00002902-0000-1000-8000-00805f9b34fb";


    public static final String BASE_UUID = MY_BASE_UUID;

    // 使能蓝牙
    public static void enableBluetooth(Activity activity, int requestCode) {
        BleUtil.enableBluetooth(activity, requestCode);
    }

    // 是否支持BLE
    public static boolean isSupportBle(Context context) {
        return BleUtil.isSupportBle(context);
    }

    // 是否使能BLE
    public static boolean isBleEnable(Context context) {
        return BleUtil.isBleEnable(context);
    }
}
