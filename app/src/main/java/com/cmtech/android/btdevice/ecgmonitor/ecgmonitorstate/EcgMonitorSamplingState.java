package com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate;

import com.cmtech.android.btdevice.ecgmonitor.EcgMonitorDevice;
import com.vise.log.ViseLog;

public class EcgMonitorSamplingState implements IEcgMonitorState {
    private EcgMonitorDevice device;

    public EcgMonitorSamplingState(EcgMonitorDevice device) {
        this.device = device;
    }

    @Override
    public void start() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void stop() {
        device.stopSampleData();
        device.setState(device.getCalibratedState());
    }

    @Override
    public void onCalibrateSuccess() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void onCalibrateFailure() {
        ViseLog.i("ecgmonitor state action wrong");
    }
}
