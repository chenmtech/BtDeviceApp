package com.cmtech.android.btdeviceapp.model;

import android.support.v4.view.GravityCompat;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.IConnectSuccessCallback;

public class BLEDeviceController {
    private final MainActivity activity;
    private final MyBluetoothDevice device;
    private DeviceFragment fragment;

    public BLEDeviceController(MyBluetoothDevice device, MainActivity activity) {
        if(device == null || activity == null) {
            throw new IllegalStateException();
        }

        this.activity = activity;
        this.device = device;
        fragment = activity.createFragmentForDevice(device);
        connectDevice();
    }

    public void connectDevice() {
        DeviceState state = device.getDeviceState();

        if(state == DeviceState.CONNECT_SUCCESS || state == DeviceState.CONNECT_PROCESS || state == DeviceState.CONNECT_DISCONNECTING) return;

        device.connect(new IConnectSuccessCallback() {
            @Override
            public void doAfterConnectSuccess(MyBluetoothDevice device) {
                fragment.executeGattInitOperation();
            }
        });
    }

    public void disconnectDevice() {
        DeviceState state = device.getDeviceState();
        if(state == DeviceState.CONNECT_DISCONNECT || state == DeviceState.CONNECT_DISCONNECTING || state == DeviceState.CONNECT_PROCESS) return;

        device.disconnect();
    }
}
