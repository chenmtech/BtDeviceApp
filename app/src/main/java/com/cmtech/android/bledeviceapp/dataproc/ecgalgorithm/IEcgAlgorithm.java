package com.cmtech.android.bledeviceapp.dataproc.ecgalgorithm;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;

public interface IEcgAlgorithm {
    String getVer();
    EcgReport process(BleEcgRecord record);
}
