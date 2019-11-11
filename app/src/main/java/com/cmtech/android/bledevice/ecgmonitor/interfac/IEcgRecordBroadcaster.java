package com.cmtech.android.bledevice.ecgmonitor.interfac;

public interface IEcgRecordBroadcaster {
    void create(String deviceId, String creatorId, int sampleRate, int caliValue, int leadTypeCode);
    void stop();
    void sendEcgSignal(int ecgSignal);
    void sendHrValue(short hr);
    void sendComment(String commenterId, String content);
    void addReceiver(String receiverId);
}
