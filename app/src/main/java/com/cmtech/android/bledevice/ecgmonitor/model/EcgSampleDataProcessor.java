package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecg1mvcalivaluecalculate.Ecg1mVCaliValueCalculator;
import com.vise.log.ViseLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

class EcgSampleDataProcessor {
    private static final int PACKAGE_NUM_MAX_LIMIT = 16;

    private int wantPackageNum = 0;

    private Ecg1mVCaliValueCalculator caliValueCalculator; // 1mV标定值计算器

    private EcgProcessor signalProcessor; // 心电信号处理器

    private ArrayList<int[]> cache = new ArrayList<>();

    EcgSampleDataProcessor() {
        for(int i = 0; i < 16; i++) {
            cache.add(null);
        }
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

    synchronized void addData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int num = (int) buffer.getShort();

        int[] tmp = new int[data.length/2-1];

        for(int i = 0; i < tmp.length; i++) {
            tmp[i] = (int)buffer.getShort();
        }

        cache.set(num, tmp);

        notifyAll();
    }


    synchronized void processEcgSignalData() throws InterruptedException{
        while(cache.get(wantPackageNum) == null) {
            wait();
        }

        int[] data = cache.get(wantPackageNum);

        for (int i = 0; i < data.length; i++) {
            signalProcessor.process(data[i]);
        }

        cache.set(wantPackageNum, null);

        if (++wantPackageNum == PACKAGE_NUM_MAX_LIMIT) wantPackageNum = 0;
    }

    synchronized void resetWantPackageNum() {
        wantPackageNum = 0;
    }

}
