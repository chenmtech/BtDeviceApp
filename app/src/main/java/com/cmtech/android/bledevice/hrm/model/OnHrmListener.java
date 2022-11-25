package com.cmtech.android.bledevice.hrm.model;

import com.cmtech.android.bledeviceapp.data.record.BleHrRecord;

/**
 * ProjectName:    BtDeviceApp
 * ClassName:      OnHrmListener
 * Description:    HRM设备监听器
 * Author:         作者名
 * CreateDate:     2020-02-04 09:35
 * UpdateUser:     更新者
 * UpdateDate:     2020-02-04 09:35
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface OnHrmListener {
    // 心率数据更新
    void onHRUpdated(BleHeartRateData hrData);

    // 心率统计信息更新
    void onHRStatisticInfoUpdated(BleHrRecord record);

    // 设备传感器位置更新
    void onHRSensLocUpdated(int loc);

    // control point更新，见蓝牙协议
    void onHRCtrlPtUpdated(int ctrl);

    // UI更新
    void onUIUpdated(int sampleRate, int gain, boolean inHrMode);

    // 心率记录状态更新
    void onHRRecordStatusUpdated(boolean record);

    // ECG信号显示
    void onEcgSignalShowed(int ecgSignal);

    // ECG信号记录状态更新
    void onEcgSignalRecordStatusUpdated(boolean record);


    void onEcgOnStatusUpdated(boolean ecgOn);
    void onEcgRecordTimeUpdated(int second);

    // 心律异常检测结果信息更新
    void onEcgRhythmDetectInfoUpdated(int rhythmLabel, String rhythmInfo);
}
