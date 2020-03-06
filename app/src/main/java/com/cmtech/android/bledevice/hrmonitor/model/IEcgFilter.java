package com.cmtech.android.bledevice.hrmonitor.model;

/**
 * IEcgFilter: Ecg filter interface
 * Created by Chenm, 2018-11-29
 */

public interface IEcgFilter {
    double filter(double ecgSignal); // filter process
    void design(int sampleRate); // design with different sample rate
}
