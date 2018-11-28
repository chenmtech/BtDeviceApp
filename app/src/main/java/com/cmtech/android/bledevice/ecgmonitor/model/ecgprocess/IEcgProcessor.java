package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

public interface IEcgProcessor {
    void init(int sampleRate, int value1mV);
    void process(int ecgSignal);
}
