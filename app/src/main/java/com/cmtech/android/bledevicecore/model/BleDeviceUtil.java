package com.cmtech.android.bledevicecore.model;

import android.app.Activity;
import android.content.Context;

import com.cmtech.android.ble.ViseBle;
import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.callback.scan.FilterScanCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.BleUtil;
import com.cmtech.android.bledeviceapp.MyApplication;

import java.util.List;

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

    public static void startScan(FilterScanCallback scanCallback) {
        ViseBle.getInstance().startScan(scanCallback);
    }

    public static void connect(BluetoothLeDevice bluetoothLeDevice, IConnectCallback connectCallback) {
        ViseBle.getInstance().connect(bluetoothLeDevice, connectCallback);
    }

    // 断开设备连接
    public static void disconnect(BleDevice device) {
        if(device != null)
            ViseBle.getInstance().getDeviceMirrorPool().disconnect(device.getBluetoothLeDevice());
    }

    public static void removeDeviceMirror(DeviceMirror deviceMirror) {
        ViseBle.getInstance().getDeviceMirrorPool().removeDeviceMirror(deviceMirror);
    }

    public static synchronized List<BluetoothLeDevice> getDeviceList() {
        return ViseBle.getInstance().getDeviceMirrorPool().getDeviceList();
    }

    public static DeviceMirror getDeviceMirror(BleDevice device) {
        if(device == null) return null;
        return ViseBle.getInstance().getDeviceMirrorPool().getDeviceMirror(device.getBluetoothLeDevice());
    }

    public static void disconnectAllDevice() {
        ViseBle.getInstance().disconnect();
    }

    public static void clearAllDevice() {
        ViseBle.getInstance().clear();
    }
}