package com.cmtech.android.bledevice.eeg.model;

import com.cmtech.android.bledevice.hrm.model.BleHeartRateData;
import com.cmtech.android.bledevice.record.BleHrRecord10;

import java.util.List;

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
public interface OnEegListener {
    void onFragmentUpdated(int sampleRate, int value1mV, double zeroLocation); // fragment updated
    void onEegSignalShowed(int eegSignal); // eeg signal showed
    void onEegSignalRecorded(boolean isRecord); // eeg signal recorded
    void onEegRecordTimeUpdated(int second);
}
