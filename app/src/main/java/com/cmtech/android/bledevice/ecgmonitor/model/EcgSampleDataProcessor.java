package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecg1mvcalivaluecalculate.Ecg1mVCaliValueCalculator;
import com.vise.log.ViseLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class EcgSampleDataProcessor {
    private static final int PACKAGE_NUM_MAX_LIMIT = 16;

    private int wantPackageNum = 0;

    private Ecg1mVCaliValueCalculator caliValueCalculator; // 1mV标定值计算器

    private EcgProcessor signalProcessor; // 心电信号处理器

    EcgSampleDataProcessor() {

    }

    void setCaliValueCalculator(Ecg1mVCaliValueCalculator caliValueCalculator) {
        this.caliValueCalculator = caliValueCalculator;
    }

    void setSignalProcessor(EcgProcessor signalProcessor) {
        this.signalProcessor = signalProcessor;
    }

    void processCalibrateData(byte[] data) throws InterruptedException{

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int[] tmp = new int[data.length/2];

        for(int i = 0; i < tmp.length; i++) {
            tmp[i] = (int)buffer.getShort();
        }

        processCalibrateData(tmp);
    }

    private synchronized void processCalibrateData(int[] data) throws InterruptedException{
        while(data[0] != wantPackageNum) {
            wait();
        }

        for (int i = 1; i < data.length; i++) {
            caliValueCalculator.process(data[i]);
        }

        if (++wantPackageNum == PACKAGE_NUM_MAX_LIMIT) wantPackageNum = 0;

        notifyAll();
    }

    void processEcgSignalData(byte[] data) throws InterruptedException {

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int[] tmp = new int[data.length/2];

        for(int i = 0; i < tmp.length; i++) {
            tmp[i] = (int)buffer.getShort();
        }

        processEcgSignalData(tmp);
    }

    private synchronized void processEcgSignalData(int[] data) throws InterruptedException{
        while(data[0] != wantPackageNum) {
            wait();
            ViseLog.e("Waiting package: " + wantPackageNum);
        }

        for (int i = 1; i < data.length; i++) {
            signalProcessor.process(data[i]);
        }

        if (++wantPackageNum == PACKAGE_NUM_MAX_LIMIT) wantPackageNum = 0;

        notifyAll();
    }

    synchronized void resetWantPackageNum() {
        wantPackageNum = 0;
    }

}
