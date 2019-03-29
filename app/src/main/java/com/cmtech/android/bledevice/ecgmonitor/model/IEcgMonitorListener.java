package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;

import java.util.List;
import java.util.Map;

public interface IEcgMonitorListener {
    // 更新设备状态
    void onUpdateState(EcgMonitorState state);
    // 更新采样率
    void onUpdateSampleRate(int sampleRate);
    // 更新导联类型
    void onUpdateLeadType(EcgLeadType leadType);
    // 更新标定值
    void onUpdateCalibrationValue(int calibrationValueBefore, int calibrationValueAfter);
    // 更新记录状态
    void onUpdateEcgSignalRecordStatus(boolean isRecord);
    // 更新EcgView
    void onUpdateEcgView(int xPixelPerData, float yValuePerPixel, int gridPixels);
    // 更新Ecg信号
    void onUpdateEcgSignal(int ecgSignal);
    // 更新记录时长，单位秒
    void onUpdateEcgSignalRecordSecond(int second);
    // 更新心率值，单位bpm
    void onUpdateEcgHr(int hr);
    // 更新心率信息
    void onUpdateEcgHrInfo(List<Integer> filteredHrList, List<EcgHrRecorder.HrHistogramElement<Float>> normHistogram, int maxHr, int averageHr);
    // 处理心率异常
    void onNotifyHrAbnormal();
}
