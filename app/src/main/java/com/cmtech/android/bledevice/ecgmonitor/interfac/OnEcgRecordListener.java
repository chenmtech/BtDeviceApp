package com.cmtech.android.bledevice.ecgmonitor.interfac;

public interface OnEcgRecordListener {
    void onRecordCreated(String recordName); // 创建新记录
    void onRecordNameSetup(String recordName); // 设置记录名，unique
    void onCreateTimeUpdated(long createTime); // 更新创建时间
    void onCreatorUpdated(String creatorId); // 更新创建人
    void onModifyTimeUpdated(long modifyTime); // 更新修改时间
    void onSampleRateUpdated(int sampleRate); // 更新采样率
    void onCaliValueUpdated(int caliValue); // 更新标定值
    void onDeviceAddressUpdated(String address); // 更新设备地址
    void onLeadTypeUpdated(int leadTypeCode); // 更新导联类型
    void onEcgSignalUpdated(int ecgSignal); // 更新信号值
    void onHrValueUpdated(short hr); // 更新心率值
    void onStart(); // 开始记录
    void onStop(); // 停止记录
}
