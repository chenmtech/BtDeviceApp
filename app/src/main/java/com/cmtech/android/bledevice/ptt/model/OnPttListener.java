package com.cmtech.android.bledevice.ptt.model;

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

public interface OnPttListener {
    void onFragmentUpdated(int sampleRate, final int ecgCaliValue, final int ppgCaliValue, float zeroLocation); // fragment updated
    void onPttSignalShowed(int ecgSignal, int ppgSignal); // PTT signal showed
    void onPttSignalRecordStatusChanged(boolean isRecord); // ptt signal record status changed
    void onPttSignalRecordTimeUpdated(int second); // ptt signal record time updated
    void onPttSignalShowStatusUpdated(boolean isShow); // ptt signal show status updated

    void onPttValueShowed(int ptt); // ptt value showed
}
