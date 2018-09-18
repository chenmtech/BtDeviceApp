package com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate;

public interface IEcgMonitorState {
    void start();
    void stop();
    void onCalibrateSuccess();
    void onCalibrateFailure();
}
