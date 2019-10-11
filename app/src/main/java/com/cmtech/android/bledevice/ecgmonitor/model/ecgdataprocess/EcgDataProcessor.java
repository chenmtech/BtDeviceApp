package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor;
import com.vise.log.ViseLog;

import java.util.List;
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

    private final EcgMonitorDevice device;
    private final Value1mVDetector value1MVDetector; // 定标前1mV值检测器
    private final EcgSignalProcessor signalProcessor; // 心电信号处理器
    private int nextPackageNum = INVALID_PACKAGE_NUM; // 下一个要处理的数据包序号
    private ExecutorService service; // 数据处理Service

    public EcgDataProcessor(EcgMonitorDevice device) {
        this.device = device;
        value1MVDetector = new Value1mVDetector(device);
        signalProcessor = new EcgSignalProcessor(device);
    }

    public void resetValue1mVCalculator() {
        this.value1MVDetector.reset();
    }

    public void resetSignalProcessor() {
        signalProcessor.reset();
    }

    public void resetHrAbnormalProcessor() {
        signalProcessor.resetHrAbnormalProcessor();
    }

    public void resetHrStatisticProcessor() {
        signalProcessor.resetHrStatisticProcessor();
    }

    public List<Short> getHrList() {
        return signalProcessor.getHrList();
    }

    public synchronized void start() {
        nextPackageNum = 0;
        if(service == null || service.isTerminated()) {
            service = Executors.newSingleThreadExecutor(new ThreadFactory() {
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

        ExecutorUtil.shutdownNowAndAwaitTerminate(service);
    }

    public synchronized void processData(final byte[] data, final boolean isCalibrationData) {
        if(service != null && !service.isTerminated()) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    int packageNum = (short)((0xff & data[0]) | (0xff00 & (data[1] << 8)));
                    if(packageNum == nextPackageNum) {
                        int[] pack = resolveDataToPackage(data);
                        for (int ele : pack) {
                            if(isCalibrationData) {
                                value1MVDetector.process(ele);
                            } else {
                                signalProcessor.process(ele);
                            }
                        }
                        if (++nextPackageNum == MAX_PACKAGE_NUM) nextPackageNum = 0;
                    } else {
                        if(nextPackageNum != INVALID_PACKAGE_NUM) {
                            ViseLog.e("数据包丢失！！！");
                            nextPackageNum = INVALID_PACKAGE_NUM;
                            device.startDisconnect();
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
