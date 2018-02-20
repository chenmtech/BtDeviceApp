package com.cmtech.android.btdeviceapp.scan;

import com.cmtech.android.ble.callback.scan.IScanCallback;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.BluetoothLeDeviceStore;
import com.cmtech.android.btdeviceapp.activity.AddDeviceActivity;

/**
 * Created by bme on 2018/2/8.
 */

public class ScanDeviceCallback implements IScanCallback {
    private AddDeviceActivity activity;

    public ScanDeviceCallback(AddDeviceActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
        if(activity != null && bluetoothLeDevice != null) {
            activity.addScanedDevice(bluetoothLeDevice);
        }
    }

    @Override
    public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {

    }

    @Override
    public void onScanTimeout() {

    }
}
