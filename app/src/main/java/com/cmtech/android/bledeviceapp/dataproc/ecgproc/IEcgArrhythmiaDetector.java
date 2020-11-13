package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;

public interface IEcgArrhythmiaDetector {
    String getVer();
    EcgReport process(BleEcgRecord record);
}
