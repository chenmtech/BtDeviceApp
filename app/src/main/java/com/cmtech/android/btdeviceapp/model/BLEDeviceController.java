package com.cmtech.android.btdeviceapp.model;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragmentFactory;
import com.cmtech.android.btdeviceapp.interfa.IConnectSuccessCallback;

public class BLEDeviceController {
    private final MainActivity activity;
    private final BLEDeviceModel device;
    private DeviceFragment fragment;

    public BLEDeviceController(BLEDeviceModel device, MainActivity activity) {
        if(device == null || activity == null) {
            throw new IllegalStateException();
        }

        this.activity = activity;
        this.device = device;
        fragment = DeviceFragmentFactory.build(device);

        activity.addDeviceFragment(device, fragment);
        connectDevice();
    }

    public void connectDevice() {
        device.connect(new IConnectSuccessCallback() {
            @Override
            public void doAfterConnectSuccess(BLEDeviceModel device) {
                fragment.executeGattInitOperation();
            }
        });
    }

    public void disconnectDevice() {
        device.disconnect();
    }
}
