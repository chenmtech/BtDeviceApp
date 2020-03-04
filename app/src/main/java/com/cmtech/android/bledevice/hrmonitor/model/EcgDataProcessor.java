package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.vise.log.ViseLog;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
  *
  * ClassName:      EcgDataProcessor
  * Description:    ecg signal processor, including resolving the ecg data packet and filtering the data
  * Author:         chenm
  * CreateDate:     2019-06-25 05:17
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-30 05:17
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgDataProcessor {
    private static final int MAX_PACKET_NUM = 256;
    private static final int INVALID_PACKET_NUM = -1;

    private final HRMonitorDevice device;
    private final IEcgFilter ecgFilter; // ecg filter
    private int nextPackNum = INVALID_PACKET_NUM; // the number of the next packet wanted to received
    private ExecutorService procService; // ecg data process Service

    public EcgDataProcessor(HRMonitorDevice device) {
        if(device == null) {
            throw new NullPointerException("The device is null.");
        }

        this.device = device;
        ecgFilter = new EcgPreFilter(device.getSampleRate());
    }

    public void reset() {
        ecgFilter.reset(device.getSampleRate());
    }

    public synchronized void start() {
        nextPackNum = 0;
        if(procService == null || procService.isTerminated()) {
            procService = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_Ecg_Process");
                }
            });

            ViseLog.e("The ecg data processor is started.");
        }
    }

    public synchronized void stop() {
        ViseLog.e("The ecg data processor is stopped.");

        ExecutorUtil.shutdownNowAndAwaitTerminate(procService);
    }

    public synchronized void processData(final byte[] data) {
        if(procService != null && !procService.isTerminated()) {
            procService.execute(new Runnable() {
                @Override
                public void run() {
                    int packageNum = (short)((0xff & data[0]) | (0xff00 & (data[1] << 8)));
                    if(packageNum == nextPackNum) {
                        int[] pack = resolveDataToPackage(data);
                        ViseLog.i("packet no. " + packageNum + ": " + Arrays.toString(pack));
                        for (int ele : pack) {
                            device.updateEcgSignal((int) ecgFilter.filter(ele));
                        }
                        if (++nextPackNum == MAX_PACKET_NUM) nextPackNum = 0;
                    } else if(nextPackNum != INVALID_PACKET_NUM){
                        ViseLog.e("The ecg data packet is gotten rid of.");
                        nextPackNum = INVALID_PACKET_NUM;
                        device.forceDisconnect(false);
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
