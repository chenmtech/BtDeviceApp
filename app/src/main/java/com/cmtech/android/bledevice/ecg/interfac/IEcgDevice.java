package com.cmtech.android.bledevice.ecg.interfac;

import com.cmtech.android.bledevice.ecg.device.EcgConfiguration;
import com.cmtech.android.bledevice.ecg.device.EcgHttpBroadcast;
import com.cmtech.android.bledevice.ecg.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecg.enumeration.EcgMonitorState;
import com.cmtech.android.bledevice.ecg.process.hr.HrStatisticProcessor;

public interface IEcgDevice extends HrStatisticProcessor.OnHrStatisticInfoUpdatedListener {
    interface OnEcgDeviceListener extends EcgHttpBroadcast.OnEcgHttpBroadcastListener {
        void onStateUpdated(EcgMonitorState state); // 状态更新
        void onSampleRateUpdated(int sampleRate); // 采样率更新
        void onLeadTypeUpdated(EcgLeadType leadType); // 导联类型更新
        void onValue1mVUpdated(int value1mV, int value1mVAfterCalibration);  // 1mV值更新
        void onRecordStateUpdated(boolean isRecord); // 记录状态更新
        void onBroadcastStateUpdated(boolean isBroadcast); // 广播状态更新
        void onEcgViewSetup(int sampleRate, int value1mV, double zeroLocation); // 信号View设置
        void onEcgSignalUpdated(int ecgSignal); // 心电信号更新
        void onEcgSignalShowStateUpdated(boolean isStart); // 心电信号显示状态更新，开始还是停止
        void onEcgSignalRecordSecondUpdated(int second); // 心电信号记录秒数更新
        void onHrUpdated(int hr); // 心率值更新，单位bpm
        void onHrStatisticsInfoUpdated(HrStatisticsInfo hrStatisticsInfo); // 心率统计信息更新
        void onAbnormalHrNotified(); // 异常心率通知
    }

    int getSampleRate();
    EcgLeadType getLeadType();
    void setValue1mV(int value1mV);
    int getValue1mV();
    EcgConfiguration getConfig();
    void updateConfig(EcgConfiguration config);
    void setEcgMonitorListener(OnEcgDeviceListener listener);
    void removeEcgMonitorListener();
    int getRecordSecond();
    long getRecordDataNum();
    boolean isRecord();
    void setRecord(boolean record);
    boolean isSaveRecord();
    void setSaveRecord(boolean saveRecord);
    void updateSignalValue(int ecgSignal);
    void updateHrValue(short hr);
    void notifyHrAbnormal();
}
