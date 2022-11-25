package com.cmtech.android.bledevice.ppg.model;

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

public interface OnPpgListener {
    void onFragmentUpdated(int sampleRate, int gain); // fragment updated
    void onPpgSignalShowed(int ppgSignal); // ppg signal showed
    void onPpgSignalRecordStatusChanged(boolean isRecord); // ppg signal record status changed
    void onPpgSignalRecordTimeUpdated(int second); // ppg signal record time updated
    void onPpgSignalShowStatusUpdated(boolean isShow); // ppg signal show status updated
}
