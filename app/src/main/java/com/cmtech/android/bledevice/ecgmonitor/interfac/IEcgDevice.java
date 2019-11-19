package com.cmtech.android.bledevice.ecgmonitor.interfac;

import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.ecgmonitor.device.EcgMonitorConfiguration;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgMonitorState;
import com.cmtech.android.bledevice.ecgmonitor.process.hr.HrStatisticProcessor;
import com.cmtech.android.bledevice.ecgmonitor.process.hr.HrStatisticsInfo;

public interface IEcgDevice extends IDevice, HrStatisticProcessor.OnHrStatisticInfoUpdatedListener {
    interface OnEcgMonitorListener {
        void onStateUpdated(EcgMonitorState state); // 状态更新
        void onSampleRateUpdated(int sampleRate); // 采样率更新
        void onLeadTypeUpdated(EcgLeadType leadType); // 导联类型更新
        void onValue1mVUpdated(int value1mV, int value1mVAfterCalibration);  // 1mV值更新
        void onRecordStateUpdated(boolean isRecord); // 记录状态更新
        void onBroadcastStateUpdated(boolean isBroadcast); // 广播状态更新
        void onShowSetupUpdated(int sampleRate, int value1mV, double zeroLocation); // 信号显示设置更新
        void onEcgSignalUpdated(int ecgSignal); // 心电信号更新
        void onEcgSignalShowStateUpdated(boolean isStart); // 心电信号显示状态更新
        void onEcgSignalRecordSecondUpdated(int second); // 心电信号记录秒数更新
        void onHrUpdated(int hr); // 心率值更新，单位bpm
        void onHrStatisticsInfoUpdated(HrStatisticsInfo hrStatisticsInfo); // 心率统计信息更新
        void onHrAbnormalNotified(); // 心率值异常通知
        void onBatteryUpdated(int bat); // 电池电量更新
    }

    int getSampleRate();
    EcgLeadType getLeadType();
    void setValue1mV(int value1mV);
    int getValue1mV();
    EcgMonitorConfiguration getConfig();
    void updateConfig(EcgMonitorConfiguration config);
    void setEcgMonitorListener(OnEcgMonitorListener listener);
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
