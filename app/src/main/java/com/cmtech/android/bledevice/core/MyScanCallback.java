package com.cmtech.android.bledevice.core;

import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import com.cmtech.android.ble.callback.scan.IScanCallback;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.BluetoothLeDeviceStore;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.vise.log.ViseLog;

/**
 * MyScanCallback: 我的扫描回调类，负责扫描回调的处理
 * Created by bme on 2018/12/23.
 */

public class MyScanCallback implements IScanCallback {
    private final BleDevice device;

    MyScanCallback(BleDevice device) {
        this.device = device;
    }

    @Override
    public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
        BluetoothDevice bluetoothDevice = bluetoothLeDevice.getDevice();
        if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            Toast.makeText(MyApplication.getContext(), "该设备未绑定，无法使用。", Toast.LENGTH_SHORT).show();
            //bluetoothDevice.createBond();   // 还没有绑定，则启动绑定
            processScanResult(false);
        } else if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            device.setBluetoothLeDevice(bluetoothLeDevice);
            processScanResult(true);
        }
    }

    @Override
    public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {

    }

    @Override
    public void onScanTimeout() {
        processScanResult(false);
    }

    // 扫描结束回调处理
    private void processScanResult(boolean result) {
        ViseLog.i("onScanFinish " + result);
        if(device.isClosing())
            return;

        if(result) {
            device.setConnectState(BleDeviceConnectState.CONNECT_PROCESS);
            device.startConnect(1000); // 扫描成功，启动连接
        } else {
            device.removeCallbacksAndMessages();
            device.setConnectState(BleDeviceConnectState.CONNECT_DISCONNECT);
        }
    }
}
