package com.cmtech.android.btdevice.ecgmonitor;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;

public class EcgMonitorDeviceController extends BLEDeviceController {
    private final EcgMonitorDevice device;
    private final EcgMonitorFragment fragment;

    public EcgMonitorDeviceController(IBLEDeviceInterface device, MainActivity activity) {
        super(device, activity);
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
