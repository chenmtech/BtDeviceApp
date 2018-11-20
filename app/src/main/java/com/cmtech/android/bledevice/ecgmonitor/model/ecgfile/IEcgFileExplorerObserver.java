package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

public interface IEcgFileExplorerObserver {
    void updateFileList();

    void updateSelectFile();

    void openFile(String fileName);
}
