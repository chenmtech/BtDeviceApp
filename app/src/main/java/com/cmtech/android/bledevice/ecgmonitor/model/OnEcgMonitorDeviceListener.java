package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrInfoObject;

public interface OnEcgMonitorDeviceListener {
    void onDeviceStateUpdated(EcgMonitorState state); // 更新设备状态

    void onSampleRateChanged(int sampleRate); // 更新采样率

    void onLeadTypeChanged(EcgLeadType leadType); // 更新导联类型

    void onCalibrationValueChanged(int calibrationValueBefore, int calibrationValueAfter);  // 更新标定值

    void onSignalRecordStateUpdated(boolean isRecord); // 更新记录状态

    void onEcgViewUpdated(int xPixelPerData, float yValuePerPixel, int gridPixels); // 更新EcgView

    void onEcgSignalUpdated(int ecgSignal); // 更新Ecg信号

    void onEcgSignalShowStarted(int sampleRate); // 启动信号显示

    void onEcgSignalShowStoped(); // 停止信号显示

    void onSignalSecNumChanged(int second); // 更新信号记录秒数

    void onEcgHrChanged(int hr); // 更新心率值，单位bpm

    void onEcgHrInfoUpdated(EcgHrInfoObject hrInfoObject); // 更新心率信息

    void onNotifyHrAbnormal(); // 通知心率值异常

    void onBatteryChanged(int bat); // 电池电量改变
}
