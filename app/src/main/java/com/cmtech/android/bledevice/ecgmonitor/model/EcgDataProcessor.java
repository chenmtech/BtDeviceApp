package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecg1mvcalivaluecalculate.Ecg1mVCaliValueCalculator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgProcessor;
import com.vise.log.ViseLog;

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

class EcgDataProcessor {
    private static final int PACKAGE_NUM_MAX_LIMIT = 16;

    private Ecg1mVCaliValueCalculator caliValueCalculator; // 1mV标定值计算器

    private EcgProcessor signalProcessor; // 心电信号处理器

    private int nextPackageNum = 0; // 下一个要处理的数据包序号

    private ExecutorService service; // 数据处理Service

    EcgDataProcessor() {

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

    void start() {
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

    void stop() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(service);
    }

    void close() {
        stop();

        if(caliValueCalculator != null) {
            caliValueCalculator.close();

            caliValueCalculator = null;
        }

        if(signalProcessor != null) {
            signalProcessor.close();

            signalProcessor = null;
        }
    }

    void processCalibrateData(final byte[] data) {
        if(service != null && !service.isShutdown()) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
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
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    void processEcgData(final byte[] data) {
        if(service != null && !service.isShutdown()) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
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
                            nextPackageNum = 0;

                            throw new InterruptedException();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


}
