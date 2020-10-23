package com.cmtech.android.bledevice.eeg.model;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      OnEegListener
 * Description:    脑电设备监听器
 * Author:         作者名
 * CreateDate:     2020-06-11 09:35
 * UpdateUser:     更新者
 * UpdateDate:     2020-06-11 09:35
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public interface OnEegListener {
    void onFragmentUpdated(int sampleRate, int value1mV, float zeroLocation); // fragment updated
    void onEegSignalShowed(int eegSignal); // eeg signal showed
    void onEegSignalRecordStatusChanged(boolean isRecord); // eeg signal record status changed
    void onEegSignalRecordTimeUpdated(int second); // eeg signal record time updated
    void onEegSignalShowStatusUpdated(boolean isShow); // eeg signal show status updated
}
