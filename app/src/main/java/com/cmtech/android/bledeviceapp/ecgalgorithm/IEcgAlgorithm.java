package com.cmtech.android.bledeviceapp.ecgalgorithm;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledevice.report.EcgReport;

public interface IEcgAlgorithm {
    String getVer();
    EcgReport process(BleEcgRecord10 record);
}
