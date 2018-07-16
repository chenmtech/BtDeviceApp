package com.cmtech.android.btdevice.ecgmonitor;

import com.cmtech.android.btdeviceapp.interfa.IBleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceController;

public class EcgMonitorDeviceController extends BleDeviceController {
    private final EcgMonitorDevice device;
    private final EcgMonitorFragment fragment;

    public EcgMonitorDeviceController(IBleDevice device) {
        super(device);
        this.device = (EcgMonitorDevice) device;
        this.fragment = (EcgMonitorFragment) getFragment();
    }

    public void setEcgRecord(boolean isRecord) {
        device.setEcgRecord(isRecord);
    }

    public void setEcgFilter(boolean isFilter) {
        device.setEcgFilter(isFilter);
    }

    public void toggleSampleEcg() {
        device.toggleSampleEcg();
    }
}
