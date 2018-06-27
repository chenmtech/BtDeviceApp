package com.cmtech.android.btdevice.ecgmonitor;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.BLEDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;
import com.cmtech.android.btdeviceapp.model.BLEDeviceBasicInfo;

public class EcgMonitorDeviceFactory extends BLEDeviceAbstractFactory {
    @Override
    public BLEDeviceModel createDevice(BLEDeviceBasicInfo persistantInfo) {
        return new EcgMonitorDevice(persistantInfo);
    }

    @Override
    public BLEDeviceController createController(BLEDeviceModel device, MainActivity activity) {
        return new EcgMonitorDeviceController(device, activity);
    }

    @Override
    public BLEDeviceFragment createFragment() {
        return new EcgMonitorFragment();
    }
}
