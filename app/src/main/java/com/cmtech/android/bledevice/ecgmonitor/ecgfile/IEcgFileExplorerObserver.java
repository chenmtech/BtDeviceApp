package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

public interface IEcgFileExplorerObserver {
    void updateFileList();

    void updateSelectFile();

    void openFile(String fileName);
}
