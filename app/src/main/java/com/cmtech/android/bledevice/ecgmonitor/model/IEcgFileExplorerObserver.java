package com.cmtech.android.bledevice.ecgmonitor.model;

public interface IEcgFileExplorerObserver {
    // 更新文件列表
    void updateFileList();
    // 播放指定文件
    void play(String fileName);
}
