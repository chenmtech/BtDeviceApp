package com.cmtech.android.btdevice.ecgmonitor;

import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceController;

public class EcgMonitorDeviceController extends BleDeviceController {
    private final EcgMonitorDevice device;
    private final EcgMonitorFragment fragment;

    public EcgMonitorDeviceController(BleDevice device) {
        super(device);
        this.device = (EcgMonitorDevice) device;
        this.fragment = (EcgMonitorFragment) getFragment();
    }

    public void isRecordEcg(boolean isRecord) {
        device.setEcgRecord(isRecord);
    }

    public void isFilterEcg(boolean isFilter) {
        device.setEcgFilter(isFilter);
    }

    public void switchSampleEcg() {
        device.switchSampleEcg();
    }
}
