package com.cmtech.android.btdevice.ecgmonitor;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BleDeviceController;
import com.cmtech.android.btdeviceapp.model.MainController;

public class EcgMonitorDeviceController extends BleDeviceController {
    private final EcgMonitorDevice device;
    private final EcgMonitorFragment fragment;

    public EcgMonitorDeviceController(IBleDeviceInterface device, MainController mainController) {
        super(device, mainController);
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
