package com.cmtech.android.bledevice.ecgmonitor.process;

import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.interfac.IEcgDevice;
import com.cmtech.android.bledevice.ecgmonitor.process.signal.EcgSignalProcessor;
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

public class EcgDataProcessor {
    private static final int MAX_PACKAGE_NUM = 16;
    private static final int INVALID_PACKAGE_NUM = -1;

    private final IDevice device;
    private final Value1mVDetector value1mVDetector; // 1mV值检测器
    private final EcgSignalProcessor signalProcessor; // 心电信号处理器
    private int nextPackageNum = INVALID_PACKAGE_NUM; // 下一个待处理的数据包序号
    private ExecutorService dataProcService; // 数据处理Service

    public EcgDataProcessor(IDevice device) {
        if(device == null) {
            throw new NullPointerException("The device is null.");
        }
        if(!(device instanceof IEcgDevice)) {
            throw new IllegalArgumentException();
        }

        this.device = device;
        value1mVDetector = new Value1mVDetector((IEcgDevice)device);
        signalProcessor = new EcgSignalProcessor((IEcgDevice)device);
    }

    public void reset() {
        resetValue1mVDetector();
        resetSignalProcessor();
    }
    public void resetValue1mVDetector() {
        this.value1mVDetector.reset();
    }
    public void resetSignalProcessor() {
        signalProcessor.reset();
    }
    public void resetHrAbnormalProcessor() {
        signalProcessor.resetHrAbnormalProcessor();
    }

    public synchronized void start() {
        nextPackageNum = 0;
        if(dataProcService == null || dataProcService.isTerminated()) {
            dataProcService = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_Data_Process");
                }
            });

            ViseLog.e("启动数据处理服务");
        }
    }

    public synchronized void stop() {
        ViseLog.e("停止数据处理服务");

        ExecutorUtil.shutdownNowAndAwaitTerminate(dataProcService);
    }

    public synchronized void processData(final byte[] data, final boolean isValue1mV) {
        if(dataProcService != null && !dataProcService.isTerminated()) {
            dataProcService.execute(new Runnable() {
                @Override
                public void run() {
                    int packageNum = (short)((0xff & data[0]) | (0xff00 & (data[1] << 8)));
                    if(packageNum == nextPackageNum) {
                        int[] pack = resolveDataToPackage(data);
                        for (int ele : pack) {
                            if(isValue1mV) {
                                value1mVDetector.process(ele);
                            } else {
                                signalProcessor.process(ele);
                            }
                        }
                        if (++nextPackageNum == MAX_PACKAGE_NUM) nextPackageNum = 0;
                    } else {
                        if(nextPackageNum != INVALID_PACKAGE_NUM) {
                            ViseLog.e("数据包丢失！！！");
                            nextPackageNum = INVALID_PACKAGE_NUM;
                            device.callDisconnect(false);
                        }
                    }
                }
            });
        }
    }

    private int[] resolveDataToPackage(byte[] data) {
        int[] pack = new int[data.length / 2 - 1];
        for (int i = 1; i <= pack.length; i++) {
            pack[i - 1] = (short) ((0xff & data[i * 2]) | (0xff00 & (data[i * 2 + 1] << 8)));
        }
        return pack;
    }
}
