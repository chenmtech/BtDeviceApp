package com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate;

import com.cmtech.android.btdevice.ecgmonitor.EcgMonitorDevice;
import com.vise.log.ViseLog;

public class EcgMonitorInitialState implements IEcgMonitorState {
    private EcgMonitorDevice device;

    public EcgMonitorInitialState(EcgMonitorDevice device) {
        this.device = device;
    }

    @Override
    public void start() {
        device.setState(device.getCalibratingState());
        device.startSample1mV();
    }

    @Override
    public void stop() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void switchState() {
        start();
    }

    @Override
    public void onCalibrateSuccess() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void onCalibrateFailure() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void onProcessData(byte[] data) {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public boolean canStop() {
        return false;
    }
}
