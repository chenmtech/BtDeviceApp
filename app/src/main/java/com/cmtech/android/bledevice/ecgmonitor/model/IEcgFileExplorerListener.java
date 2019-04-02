package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;

public interface IEcgFileExplorerListener extends EcgHrRecorder.IEcgHrInfoUpdatedListener {
    void onUpdateEcgFileList(); // 更新EcgFile文件列表
}
