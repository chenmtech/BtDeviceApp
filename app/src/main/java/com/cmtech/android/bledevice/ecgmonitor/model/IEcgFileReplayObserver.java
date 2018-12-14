package com.cmtech.android.bledevice.ecgmonitor.model;

public interface IEcgFileReplayObserver {
    // 更新留言列表
    void updateCommentList();
    // 更新是否在留言中显示留言时间
    void updateShowSecondInComment(boolean show, int second);
}
