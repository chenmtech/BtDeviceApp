package com.cmtech.android.bledeviceapp.dataproc.ecgproc.afdetector;

public interface IAFDetector {
    int NON_AF = 0;
    int AF = 1;
    int UNDETERMIN = 2;

    int getClassifyResult();
}
