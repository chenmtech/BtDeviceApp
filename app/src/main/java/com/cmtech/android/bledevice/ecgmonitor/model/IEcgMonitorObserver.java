package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;

public interface IEcgMonitorObserver {
    // 更新设备状态
    void updateState(EcgMonitorState state);
    // 更新采样率
    void updateSampleRate(int sampleRate);
    // 更新导联类型
    void updateLeadType(EcgLeadType leadType);
    // 更新标定值
    void updateCalibrationValue(int calibrationValueBefore, int calibrationValueAfter);
    // 更新记录状态
    void updateRecordStatus(boolean isRecord);
    // 更新EcgView
    void updateEcgView(int xPixelPerData, float yValuePerPixel, int gridPixels);
    // 更新Ecg信号
    void updateEcgSignal(int ecgSignal);
    // 更新记录时长，单位秒
    void updateRecordSecond(int second);
    // 更新心率值，单位bpm
    void updateEcgHr(int hr);
    // 心率异常
    void hrAbnormal();
}
