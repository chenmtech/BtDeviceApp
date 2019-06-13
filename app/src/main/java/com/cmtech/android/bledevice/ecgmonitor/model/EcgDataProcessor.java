package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor;
import com.vise.log.ViseLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.LinkedBlockingQueue;

class EcgDataProcessor {
    public static final int PACKAGE_NUM_LIMIT = 16;

    private int wantPackageNum = 0;

    private EcgCalibrateDataProcessor calibrateDataProcessor; // 标定数据处理器

    private EcgSignalProcessor signalProcessor; // 心电信号处理器

    EcgDataProcessor() {

    }

    void setCalibrateDataProcessor(EcgCalibrateDataProcessor calibrateDataProcessor) {
        this.calibrateDataProcessor = calibrateDataProcessor;
    }

    void setSignalProcessor(EcgSignalProcessor signalProcessor) {
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
            calibrateDataProcessor.process(data[i]);
        }

        if (++wantPackageNum == PACKAGE_NUM_LIMIT) wantPackageNum = 0;

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

        if (++wantPackageNum == PACKAGE_NUM_LIMIT) wantPackageNum = 0;

        notifyAll();
    }

    synchronized void resetWantPackageNum() {
        wantPackageNum = 0;
    }

}
