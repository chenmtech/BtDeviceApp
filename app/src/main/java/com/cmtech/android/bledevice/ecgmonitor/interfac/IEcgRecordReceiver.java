package com.cmtech.android.bledevice.ecgmonitor.interfac;

public interface IEcgRecordReceiver {
    void request(String receiverId, String broadcastId);
    void stop(String receiverId, String broadcastId);

}
