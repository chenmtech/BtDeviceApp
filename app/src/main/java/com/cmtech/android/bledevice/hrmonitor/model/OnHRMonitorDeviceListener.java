package com.cmtech.android.bledevice.hrmonitor.model;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      OnHRMonitorDeviceListener
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020-02-04 09:35
 * UpdateUser:     更新者
 * UpdateDate:     2020-02-04 09:35
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface OnHRMonitorDeviceListener {
    void onHRUpdated(BleHeartRateData hrData); // heart rate updated
    void onHRSensLocUpdated(int loc); // sensor location updated
    void onHRCtrlPtUpdated(int ctrl); // control point updated
    void onFragmentUpdated(int sampleRate, int value1mV, double zeroLocation, boolean ecgSwitchOn); // fragment updated
    void onEcgSignalShowed(int ecgSignal); // ecg signal showed
}
