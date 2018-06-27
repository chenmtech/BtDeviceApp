package com.cmtech.android.btdevice.ecgmonitor;

import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceModelInterface;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;
import com.cmtech.dsp.bmefile.BmeFile;
import com.cmtech.dsp.exception.FileException;
import com.vise.utils.file.FileUtil;

import java.io.File;

public class EcgMonitorDeviceController extends BLEDeviceController {
    private final EcgMonitorDevice device;
    private final EcgMonitorFragment fragment;

    public EcgMonitorDeviceController(IBLEDeviceModelInterface device, MainActivity activity) {
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
