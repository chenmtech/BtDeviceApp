package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;

import java.util.List;

public interface IEcgMonitorListener {
    void onUpdateDeviceState(EcgMonitorState state); // 更新设备状态
    void onUpdateSampleRate(int sampleRate); // 更新采样率
    void onUpdateLeadType(EcgLeadType leadType); // 更新导联类型
    void onUpdateCalibrationValue(int calibrationValueBefore, int calibrationValueAfter);  // 更新标定值
    void onUpdateSignalRecordStatus(boolean isRecord); // 更新记录状态
    void onUpdateEcgView(int xPixelPerData, float yValuePerPixel, int gridPixels); // 更新EcgView
    void onUpdateEcgSignal(int ecgSignal); // 更新Ecg信号
    void onUpdateSignalSecNum(int second); // 更新信号记录秒数
    void onUpdateEcgHr(int hr); // 更新心率值，单位bpm
    void onUpdateEcgHrInfo(List<Short> filteredHrList, List<EcgHrRecorder.HrHistogramElement<Float>> normHistogram, short maxHr, short averageHr); // 更新心率信息
    void onNotifyHrAbnormal(); // 通知心率值异常
}
