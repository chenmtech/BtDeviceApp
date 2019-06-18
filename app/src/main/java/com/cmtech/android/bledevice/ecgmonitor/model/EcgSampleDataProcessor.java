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

    private int nextProcessPackageNum = 0; // 下一个要处理的数据包序号

    private int[] packNum = {1,2,3,0,5,6,7,4,9,10,11,8,13,14,15,12};

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
        while(data[0] != nextProcessPackageNum) {
            wait();
        }

        for (int i = 1; i < data.length; i++) {
            caliValueCalculator.process(data[i]);
        }

        if (++nextProcessPackageNum == PACKAGE_NUM_MAX_LIMIT) nextProcessPackageNum = 0;

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

    private void addPackage(int packageNum, int[] pack) throws InterruptedException{
        packageCache[packageNum] = pack;

        notifyAll();
    }

    synchronized void processEcgSignalData() throws InterruptedException{
        while(packageCache[nextProcessPackageNum] == null) {
            wait();
        }

        do {

            int[] data = packageCache[nextProcessPackageNum];

            for (int ele : data) {
                signalProcessor.process(ele);
            }

            packageCache[nextProcessPackageNum] = null;

            if (++nextProcessPackageNum == PACKAGE_NUM_MAX_LIMIT) nextProcessPackageNum = 0;
        }while (packageCache[nextProcessPackageNum] != null);
    }

    synchronized void reset() {
        nextProcessPackageNum = 0;

        for(int i = 0; i < PACKAGE_NUM_MAX_LIMIT; i++) {
            packageCache[i] = null;
        }
    }

}
