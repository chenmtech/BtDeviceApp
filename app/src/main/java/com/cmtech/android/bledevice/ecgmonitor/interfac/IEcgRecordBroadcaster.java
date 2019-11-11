package com.cmtech.android.bledevice.ecgmonitor.interfac;

public interface IEcgRecordBroadcaster {
    void create(String deviceId, String creatorId, int sampleRate, int caliValue, int leadTypeCode); // 创建一个心电记录广播
    void stop(); // 停止广播
    void sendEcgSignal(int ecgSignal); // 发送心电信号
    void sendHrValue(short hr); // 发送心率值
    void sendComment(String commenterId, String content); // 发送一条留言
    void addReceiver(String receiverId); // 添加一个接收者
}
