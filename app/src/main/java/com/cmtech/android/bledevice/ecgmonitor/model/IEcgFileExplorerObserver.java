package com.cmtech.android.bledevice.ecgmonitor.model;

public interface IEcgFileExplorerObserver {
    // 更新文件列表
    void update();
    // 播放指定文件
    void replay(String fileName);
}