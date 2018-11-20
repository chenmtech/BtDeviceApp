package com.cmtech.android.bledevice.ecgmonitor.model.state;

public interface IEcgMonitorState {
    void start();
    void stop();
    void switchState();
    void onCalibrateSuccess();
    void onCalibrateFailure();
    void onProcessData(byte[] data);
    boolean canStart();
    boolean canStop();
}
