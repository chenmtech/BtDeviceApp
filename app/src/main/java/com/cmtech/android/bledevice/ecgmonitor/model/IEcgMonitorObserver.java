package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.state.IEcgMonitorState;

public interface IEcgMonitorObserver {
    // 更新设备状态
    void updateState(IEcgMonitorState state);
    // 更新采样率
    void updateSampleRate(int sampleRate);
    // 更新导联类型
    void updateLeadType(EcgLeadType leadType);
    // 更新标定值
    void updateCalibrationValue(int calibrationValue);
    // 更新记录状态
    void updateRecordStatus(boolean isRecord);
    // 更新EcgView
    void updateEcgView(int xRes, float yRes, int viewGridWidth);
    // 更新Ecg信号
    void updateEcgSignal(int ecgSignal);
    // 更新记录时长，单位秒
    void updateRecordSecond(int second);
    // 更新心率值，单位bpm
    void updateEcgHr(int hr);
    // 通知心率报警
    void notifyHrWarn();
}
