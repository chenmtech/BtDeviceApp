package com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate;

import com.cmtech.android.btdevice.ecgmonitor.EcgMonitorDevice;
import com.vise.log.ViseLog;

public class EcgMonitorCalibratedState implements IEcgMonitorState {
    private EcgMonitorDevice device;

    public EcgMonitorCalibratedState(EcgMonitorDevice device) {
        this.device = device;
    }

    @Override
    public void start() {
        device.setState(device.getSamplingState());
        device.startSampleEcg();
    }

    @Override
    public void stop() {
        ViseLog.i("ecgmonitor state action wrong");
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
