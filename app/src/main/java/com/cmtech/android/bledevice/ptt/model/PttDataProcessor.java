package com.cmtech.android.bledevice.ptt.model;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledeviceapp.dataproc.PpgSignalPreFilter;
import com.cmtech.android.bledeviceapp.dataproc.ISignalFilter;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;
import com.vise.log.ViseLog;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
  *
  * ClassName:      PttDataProcessor
  * Description:    ptt signal processor, including resolving the ptt data packet and filtering the data
  * Author:         chenm
  * CreateDate:     2020-06-11 05:17
  * UpdateUser:     chenm
  * UpdateDate:     2020-06-11 05:17
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class PttDataProcessor {
    private static final int MAX_PACKET_NUM = 255; // the max packet number
    private static final int INVALID_PACKET_NUM = -1; // invalid packet number

    private final PttDevice device;
    private int nextWantedPack = INVALID_PACKET_NUM; // the next packet number wanted to received
    private final ISignalFilter ecgFilter; //ECG filter
    private final ISignalFilter ppgFilter; // PPG filter
    private final PttDetector pttDetector; // PTT Detector
    private ExecutorService procService; // PTT data process Service

    public PttDataProcessor(PttDevice device) {
        if(device == null) {
            throw new NullPointerException("The device is null.");
        }

        this.device = device;
        ecgFilter = new EcgSignalPreFilter(device.getSampleRate());
        ppgFilter = new PpgSignalPreFilter(device.getSampleRate());
        pttDetector = new PttDetector(device.getSampleRate());
    }

    public void reset() {
        ppgFilter.design(device.getSampleRate());
        ecgFilter.design(device.getSampleRate());
        pttDetector.initialize();
    }

    public synchronized void start() {
        nextWantedPack = 0;
        if(ExecutorUtil.isDead(procService)) {
            procService = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_PTT_Process");
                }
            });

            ViseLog.e("The PTT data processor is started.");
        }
    }

    public synchronized void stop() {
        ViseLog.e("The PTT data processor is stopped.");

        ExecutorUtil.shutdownNowAndAwaitTerminate(procService);
    }

    public synchronized void processData(final byte[] data) {
        if(!ExecutorUtil.isDead(procService)) {
            procService.execute(new Runnable() {
                @Override
                public void run() {
                    int packageNum = UnsignedUtil.getUnsignedByte(data[0]);
                    // 计算接收到的包号与想要的包号之间的区别，以获取丢包数量
                    int lostNum = (packageNum - nextWantedPack) % MAX_PACKET_NUM;
                    ViseLog.e("wanted: " + nextWantedPack + "received: " + packageNum + ", isEqual: " + (nextWantedPack == packageNum));
                    // 如果丢包小于6，则接受
                    if(lostNum < 6) { // the packet number is ok
                        parseAndProcessDataPacket(data, 1, lostNum+1);
                        if(packageNum == MAX_PACKET_NUM)
                            nextWantedPack = 0;
                        else
                            nextWantedPack = packageNum+1;
                    } else if(nextWantedPack != INVALID_PACKET_NUM){ // bad packet, force disconnect
                        ViseLog.e("The PTT data packet is lost too much. Disconnect device.");
                        nextWantedPack = INVALID_PACKET_NUM;
                        device.disconnect(false);
                    }
                }
            });
        }
    }

    private void parseAndProcessDataPacket(byte[] data, int begin, int repeat) {
        int n = (data.length-begin)/4;
        int[] ecgData = new int[n];
        int[] ppgData = new int[n];
        for (int i = begin, j = 0; i < data.length; i=i+4, j++) {
            ecgData[j] = (short) ((0xff & data[i]) | (0xff00 & (data[i+1] << 8))); // the type of ECG Data is int16
            ppgData[j] = ByteUtil.getInt(new byte[]{data[i+2], data[i+3], 0x00, 0x00}); // the type of PPG Data is uint16
            for(int t = 0; t < repeat; t++) {
                int ecg = (int) Math.round(ecgFilter.filter(ecgData[j]));
                int ppg = -(int) Math.round(ppgFilter.filter(ppgData[j]));
                device.showPttSignal(ecg, ppg);
                device.recordPttSignal(ecg, ppg);
                int ptt = pttDetector.findFootPtt(ecg, ppg);
                if (ptt != 0) {
                    device.processPtt(ptt);
                }
            }
        }
        ViseLog.i("ECG Data: " + Arrays.toString(ecgData));
        ViseLog.i("PPG Data: " + Arrays.toString(ppgData));
    }
}
