package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecg1mvcalivaluecalculate.Ecg1mVCaliValueCalculator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgProcessor;
import com.vise.log.ViseLog;

class EcgSampleDataProcessor {
    private static final int PACKAGE_NUM_MAX_LIMIT = 16;

    private Ecg1mVCaliValueCalculator caliValueCalculator; // 1mV标定值计算器

    private EcgProcessor signalProcessor; // 心电信号处理器

    private int nextPackageNum = 0; // 下一个要处理的数据包序号

    EcgSampleDataProcessor() {

    }

    void setCaliValueCalculator(Ecg1mVCaliValueCalculator caliValueCalculator) {
        this.caliValueCalculator = caliValueCalculator;
    }

    EcgProcessor getSignalProcessor() {
        return signalProcessor;
    }

    void setSignalProcessor(EcgProcessor signalProcessor) {
        this.signalProcessor = signalProcessor;
    }

    void processCalibrateData(byte[] data) throws InterruptedException{
        int packageNum = (short)((0xff & data[0]) | (0xff00 & (data[1] << 8)));

        if(packageNum == nextPackageNum) {
            int[] pack = new int[data.length / 2 - 1];

            for (int i = 1; i <= pack.length; i++) {
                pack[i - 1] = (short)((0xff & data[i * 2]) | (0xff00 & (data[i * 2 + 1] << 8)));
            }

            //ViseLog.e("Calibrate Data: " + Arrays.toString(pack));

            for (int ele : pack) {
                caliValueCalculator.process(ele);
            }

            if (++nextPackageNum == PACKAGE_NUM_MAX_LIMIT) nextPackageNum = 0;
        } else {
            nextPackageNum = 0;

            throw new InterruptedException();
        }
    }

    void processEcgData(byte[] data) throws InterruptedException{
        int packageNum = (short)((0xff & data[0]) | (0xff00 & (data[1] << 8)));

        if(packageNum == nextPackageNum) {
            int[] pack = new int[data.length / 2 - 1];

            for (int i = 1; i <= pack.length; i++) {
                pack[i - 1] = (short)((0xff & data[i * 2]) | (0xff00 & (data[i * 2 + 1] << 8)));
            }

            for (int ele : pack) {
                signalProcessor.process(ele);
            }

            if (++nextPackageNum == PACKAGE_NUM_MAX_LIMIT) nextPackageNum = 0;
        } else {
            nextPackageNum = 0;

            throw new InterruptedException();
        }
    }

    void resetPackageNum() {
        nextPackageNum = 0;
    }

    void close() {
        if(caliValueCalculator != null) {
            caliValueCalculator.close();
            caliValueCalculator = null;
        }

        if(signalProcessor != null) {
            signalProcessor.close();
            signalProcessor = null;
            ViseLog.e("signal processor is null");
        }
    }

}
