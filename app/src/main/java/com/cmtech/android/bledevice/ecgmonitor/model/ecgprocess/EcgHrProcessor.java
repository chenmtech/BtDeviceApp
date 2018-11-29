package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;

public class EcgHrProcessor implements IEcgProcessor {
    private QrsDetector qrsDetector;

    private int hr = 0;
    public int getHr() {
        return hr;
    }

    public EcgHrProcessor(int sampleRate, int value1mV) {
        qrsDetector = new QrsDetector(sampleRate, value1mV);
    }

    @Override
    public void process(int ecgSignal) {
        hr = qrsDetector.outputHR(ecgSignal);
    }
}
