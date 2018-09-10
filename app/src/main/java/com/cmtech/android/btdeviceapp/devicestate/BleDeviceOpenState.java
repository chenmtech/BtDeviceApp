package com.cmtech.android.btdeviceapp.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.vise.log.ViseLog;

import static com.cmtech.android.btdeviceapp.model.BleDeviceConnectState.CONNECT_SUCCESS;

public class BleDeviceOpenState implements IBleDeviceState {
    BleDevice device;

    public BleDeviceOpenState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void deviceOpen() {
        ViseLog.i("action wrong");
    }

    @Override
    public void deviceClose() {
        device.setState(device.getCloseState());
    }

    @Override
    public void deviceStartScan() {
        MyApplication.getViseBle().connectByMac(device.getMacAddress(), device.getConnectCallback());
        device.setState(device.getScanState());
    }

    @Override
    public void deviceDisconnect() {
        ViseLog.i("action wrong");
    }

    @Override
    public void onDeviceScanSuccess() {
        ViseLog.i("state wrong");
    }

    @Override
    public void onDeviceScanFailure() {
        ViseLog.i("state wrong");
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        device.setBluetoothLeDevice(mirror.getBluetoothLeDevice());

        ViseLog.i("onConnectSuccess");

        device.getHandler().post(new Runnable() {
            @Override
            public void run() {
                device.processConnectCallback();
            }
        });

        device.setState(device.getConnectedState());
    }

    @Override
    public void onDeviceConnectFailure() {
        ViseLog.i("state wrong");
    }

    @Override
    public void onDeviceConnectTimeout() {
        ViseLog.i("state wrong");
    }

    @Override
    public void onDeviceDisconnect() {
        ViseLog.i("state wrong");
    }

    @Override
    public String deviceGetStateInfo() {
        return "等待连接";
    }
}
