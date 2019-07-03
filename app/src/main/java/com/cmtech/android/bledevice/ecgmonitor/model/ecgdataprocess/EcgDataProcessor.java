package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
  *
  * ClassName:      EcgDataProcessor
  * Description:    数据处理器，包含1mV定标数据和心电信号的处理
  * Author:         chenm
  * CreateDate:     2019-06-25 05:17
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-30 05:17
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgDataProcessor {
    private static final int PACKAGE_NUM_MAX_LIMIT = 16;
    private static final int INVALID_PACKAGE_NUM = -1;

    private final EcgMonitorDevice device;

    private Ecg1mVCaliValueCalculator caliValueCalculator; // 1mV标定值计算器

    private final EcgSignalProcessor signalProcessor; // 心电信号处理器

    private int nextPackageNum = INVALID_PACKAGE_NUM; // 下一个要处理的数据包序号

    private ExecutorService service; // 数据处理Service

    public EcgDataProcessor(EcgMonitorDevice device) {
        this.device = device;

        signalProcessor = new EcgSignalProcessor(device, device.getValue1mVAfterCalibration());
    }

    public void setCaliValueCalculator(Ecg1mVCaliValueCalculator caliValueCalculator) {
        this.caliValueCalculator = caliValueCalculator;
    }

    public EcgSignalProcessor getSignalProcessor() {
        return signalProcessor;
    }

    public void start() {
        nextPackageNum = 0;

        if(service == null || service.isTerminated()) {
            service = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_Data_Process");
                }
            });
        }
    }

    public void stop() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(service);
    }

    public void close() {
        stop();

        if(caliValueCalculator != null) {
            caliValueCalculator.close();

            caliValueCalculator = null;
        }

        if(signalProcessor != null) {
            signalProcessor.close();

            //signalProcessor = null;
        }
    }

    public void processCalibrateData(final byte[] data) {
        if(service != null && !service.isShutdown()) {
            service.execute(new Runnable() {
                @Override
                public void run() {
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
                        if(nextPackageNum != INVALID_PACKAGE_NUM) {
                            nextPackageNum = INVALID_PACKAGE_NUM;

                            device.stopDataSampling();

                            device.startEcgSignalSampling();
                        }

                    }
                }
            });
        }
    }

    public void processEcgData(final byte[] data) {
        if(service != null && !service.isShutdown()) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    int packageNum = (short) ((0xff & data[0]) | (0xff00 & (data[1] << 8)));

                    if (packageNum == nextPackageNum) {
                        int[] pack = new int[data.length / 2 - 1];

                        for (int i = 1; i <= pack.length; i++) {
                            pack[i - 1] = (short) ((0xff & data[i * 2]) | (0xff00 & (data[i * 2 + 1] << 8)));
                        }

                        for (int ele : pack) {
                            signalProcessor.process(ele);
                        }

                        if (++nextPackageNum == PACKAGE_NUM_MAX_LIMIT) nextPackageNum = 0;
                    } else {
                        if(nextPackageNum != INVALID_PACKAGE_NUM) {
                            nextPackageNum = INVALID_PACKAGE_NUM;

                            device.stopDataSampling();

                            device.startEcgSignalSampling();
                        }
                    }
                }
            });
        }
    }


}
