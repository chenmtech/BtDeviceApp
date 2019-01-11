package com.cmtech.android.bledevice.ecgmonitor.model;

public interface IEcgFileReplayObserver {
    // 更新附加信息列表
    void updateAppendixList();
    // 更新是否在留言中显示留言时间
    void updateShowSecondInComment(boolean show, int second);
}
