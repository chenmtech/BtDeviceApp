package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.HrStatisticProcessor;

public interface OnEcgFileExploreUpdatedListener extends EcgFilesManager.OnEcgFilesChangeListener, HrStatisticProcessor.OnHrStatisticInfoUpdatedListener {

}
