package com.cmtech.android.bledevice.hrm.model;

import com.cmtech.android.bledeviceapp.data.record.BleHrRecord10;

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
public interface OnHrmListener {
    void onHRUpdated(BleHeartRateData hrData); // heart rate updated
    void onHRStatisticInfoUpdated(BleHrRecord10 record);
    void onHRSensLocUpdated(int loc); // sensor location updated
    void onHRCtrlPtUpdated(int ctrl); // control point updated
    void onFragmentUpdated(int sampleRate, int value1mV, float zeroLocation, boolean inHrMode); // fragment updated
    void onHrRecordStatusUpdated(boolean record);
    void onEcgSignalShowed(int ecgSignal); // ecg signal showed
    void onEcgSignalRecordStatusUpdated(boolean record); // ecg signal recorded
    void onEcgOnStatusUpdated(boolean ecgOn);
    void onEcgRecordTimeUpdated(int second);
}
