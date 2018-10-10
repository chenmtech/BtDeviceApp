package com.cmtech.android.bledevice.ecgmonitor;

import com.cmtech.android.bledeviceapp.activity.DeviceBasicInfoActivity;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledeviceapp.model.BleDeviceController;

public class EcgMonitorController extends BleDeviceController {
    private final EcgMonitorDevice device;
    private final EcgMonitorFragment fragment;

    public EcgMonitorController(BleDevice device) {
        super(device);
        this.device = (EcgMonitorDevice) device;
        this.fragment = (EcgMonitorFragment) getFragment();
    }

    // 转换采样状态
    public void switchSampleState() {
        device.switchSampleState();
    }

    // 设置是否记录心电信号
    public void setEcgRecord(boolean isRecord) {
        device.setEcgRecord(isRecord);
    }

    // 设置是否对心电信号进行滤波处理
    public void setEcgFilter(boolean isFilter) {
        device.setEcgFilter(isFilter);
    }


}
