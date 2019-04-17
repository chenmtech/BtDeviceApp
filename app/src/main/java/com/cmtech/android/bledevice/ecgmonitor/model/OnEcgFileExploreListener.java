package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrProcessor;

public interface OnEcgFileExploreListener extends EcgFilesManager.OnEcgFilesChangeListener, EcgHrProcessor.OnEcgHrInfoUpdateListener {

}
