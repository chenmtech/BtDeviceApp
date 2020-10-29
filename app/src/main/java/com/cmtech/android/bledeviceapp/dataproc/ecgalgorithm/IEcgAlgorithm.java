package com.cmtech.android.bledeviceapp.dataproc.ecgalgorithm;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord10;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;

public interface IEcgAlgorithm {
    String getVer();
    EcgReport process(BleEcgRecord10 record);
}
