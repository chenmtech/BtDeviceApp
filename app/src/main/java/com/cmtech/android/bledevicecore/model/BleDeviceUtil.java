package com.cmtech.android.bledevicecore.model;

import android.app.Activity;
import android.content.Context;

import com.cmtech.android.ble.utils.BleUtil;

public class BleDeviceUtil {
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
