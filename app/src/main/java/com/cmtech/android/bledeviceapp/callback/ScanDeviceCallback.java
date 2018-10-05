package com.cmtech.android.bledeviceapp.callback;

import com.cmtech.android.ble.callback.scan.IScanCallback;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.BluetoothLeDeviceStore;
import com.cmtech.android.bledeviceapp.activity.ScanDeviceActivity;

/**
 * Created by bme on 2018/2/8.
 */

public class ScanDeviceCallback implements IScanCallback {
    private ScanDeviceActivity activity;

    public ScanDeviceCallback(ScanDeviceActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
        if(activity != null && bluetoothLeDevice != null) {
            activity.addOneNewDeviceToFoundDeviceList(bluetoothLeDevice);
        }
    }

    @Override
    public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {

    }

    @Override
    public void onScanTimeout() {

    }
}
