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
    void onHrDataUpdated(BleHeartRateData hrData);

    // 心率统计信息更新
    void onHrStatisticInfoUpdated(BleHrRecord record);

    // 设备传感器位置更新
    void onHrSensLocUpdated(int loc);

    // control point更新，见蓝牙协议
    void onHrCtrlPtUpdated(int ctrl);

    // UI更新
    void onUIUpdated(int sampleRate, int gain, boolean inHrMode);

    // 心率记录状态更新
    void onHrRecordStatusUpdated(boolean record);

    // ECG信号显示
    void onEcgSignalShowed(int ecgSignal);

    // ECG信号记录状态更新
    void onEcgSignalRecordStatusUpdated(boolean record);

    // ECG 功能开关状态更新
    void onEcgOnStatusUpdated(boolean ecgOn);

    // ECG信号记录时长
    void onEcgRecordTimeUpdated(int second);

    // ECG注解更新
    void onEcgAnnotationUpdated(String annSymbol, String annContent);
}
