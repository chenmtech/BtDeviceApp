package com.cmtech.android.bledevice.ecgmonitor;

public interface OnEcgRecordListener {
    void onRecordCreated();
    void onRecordNameSetup(String recordName);
    void onCreateTimeUpdated(long createTime);
    void onCreatorUpdated(String creatorId);
    void onModifyTimeUpdated(long modifyTime);
    void onSampleRateUpdated(int sampleRate);
    void onCaliValueUpdated(int caliValue);
    void onDeviceAddressUpdated(String address);
    void onLeadTypeCodeUpdated(int leadTypeCode);
    void onHrValueUpdated(short hr);
    void onStart();
    void onStop();
}
