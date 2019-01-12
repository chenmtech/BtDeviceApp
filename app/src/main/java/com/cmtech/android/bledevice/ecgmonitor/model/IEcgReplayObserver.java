package com.cmtech.android.bledevice.ecgmonitor.model;

public interface IEcgReplayObserver {
    // 更新附加信息列表
    void updateAppendixList();
    // 更新是否在附加信息中显示时间
    void updateIsShowTimeInAppendix(boolean show, int second);
}
