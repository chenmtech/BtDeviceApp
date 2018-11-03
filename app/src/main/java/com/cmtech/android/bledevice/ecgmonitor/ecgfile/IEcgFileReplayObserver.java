package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

public interface IEcgFileReplayObserver {
    void updateCommentList();

    void initEcgView(int xRes, float yRes, int viewGridWidth, double zerolocation);

}
