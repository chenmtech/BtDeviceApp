package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecg1mvcalivaluecalculate.Ecg1mVCaliValueCalculator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgProcessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class EcgSampleDataProcessor {
    private static final int PACKAGE_NUM_MAX_LIMIT = 16;

    private Ecg1mVCaliValueCalculator caliValueCalculator; // 1mV标定值计算器

    private EcgProcessor signalProcessor; // 心电信号处理器

    private int[][] packageCache = new int[PACKAGE_NUM_MAX_LIMIT][]; // 数据包缓存

    private int nextPackageNum = 0; // 下一个要处理的数据包序号

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
        while(data[0] != nextPackageNum) {
            wait();
        }

        for (int i = 1; i < data.length; i++) {
            caliValueCalculator.process(data[i]);
        }

        if (++nextPackageNum == PACKAGE_NUM_MAX_LIMIT) nextPackageNum = 0;

        notifyAll();
    }

    synchronized void addData(byte[] data) throws InterruptedException{
        int packageNum = ((0xff & data[0]) | (0xff00 & (data[1] << 8)));

        int[] pack = new int[data.length/2-1];

        for(int i = 1; i <= pack.length; i++) {
            pack[i-1] = ((0xff & data[i*2]) | (0xff00 & (data[i*2+1] << 8)));
        }

        addPackage(packageNum, pack);
    }

    private synchronized void addPackage(int packageNum, int[] pack) throws InterruptedException{
        while(packageCache[packageNum] != null) {
            wait();
        }

        packageCache[packageNum] = pack;

        notifyAll();
    }

    synchronized void processEcgSignalData() throws InterruptedException{
        while(packageCache[nextPackageNum] == null) {
            wait();
        }

        /*do {

            int[] data = packageCache[nextPackageNum];

            for (int ele : data) {
                signalProcessor.process(ele);
            }

            packageCache[nextPackageNum] = null;

            if (++nextPackageNum == PACKAGE_NUM_MAX_LIMIT) nextPackageNum = 0;
        }while (packageCache[nextPackageNum] != null);*/

        int[] data = packageCache[nextPackageNum];

        for (int ele : data) {
            signalProcessor.process(ele);
        }

        packageCache[nextPackageNum] = null;

        if (++nextPackageNum == PACKAGE_NUM_MAX_LIMIT) nextPackageNum = 0;

        notifyAll();
    }

    synchronized void reset() {
        nextPackageNum = 0;

        for(int i = 0; i < PACKAGE_NUM_MAX_LIMIT; i++) {
            packageCache[i] = null;
        }
    }

}
