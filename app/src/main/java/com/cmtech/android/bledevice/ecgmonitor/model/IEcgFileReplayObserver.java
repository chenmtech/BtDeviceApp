package com.cmtech.android.bledevice.ecgmonitor.model;

public interface IEcgFileReplayObserver {
    // 更新留言列表
    void updateCommentList();
    // 初始化ecgView
    void initEcgView(int xRes, float yRes, int viewGridWidth, double zerolocation);

}
