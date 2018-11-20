package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

public interface IEcgFileReplayObserver {
    void updateCommentList();

    void initEcgView(int xRes, float yRes, int viewGridWidth, double zerolocation);

}
