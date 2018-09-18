package com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate;

import com.cmtech.android.btdevice.ecgmonitor.EcgMonitorDevice;
import com.vise.log.ViseLog;

public class EcgMonitorCalibratingState implements IEcgMonitorState {
    private EcgMonitorDevice device;

    public EcgMonitorCalibratingState(EcgMonitorDevice device) {
        this.device = device;
    }

    @Override
    public void start() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void stop() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void onCalibrateSuccess() {
        device.stopSampleData();
        device.setState(device.getCalibratedState());
        device.start();
    }

    @Override
    public void onCalibrateFailure() {
        device.stopSampleData();
        device.setState(device.getInitialState());
    }
}
