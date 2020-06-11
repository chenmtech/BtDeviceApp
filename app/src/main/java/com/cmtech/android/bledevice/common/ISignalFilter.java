package com.cmtech.android.bledevice.common;

/**
 * ISignalFilter: Signal filter interface
 * Created by Chenm, 2018-11-29
 */

public interface ISignalFilter {
    double filter(double signal); // filter process
    void design(int sampleRate); // design with different sample rate
}
