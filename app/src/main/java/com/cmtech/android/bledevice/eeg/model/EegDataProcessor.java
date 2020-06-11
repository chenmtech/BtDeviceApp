package com.cmtech.android.bledevice.eeg.model;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.common.SignalPreFilter;
import com.cmtech.android.bledevice.common.ISignalFilter;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;
import com.vise.log.ViseLog;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
  *
  * ClassName:      EcgDataProcessor
  * Description:    eeg signal processor, including resolving the eeg data packet and filtering the data
  * Author:         chenm
  * CreateDate:     2019-06-25 05:17
  * UpdateUser:     chenm
  * UpdateDate:     2020-03-06 05:17
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EegDataProcessor {
    private static final int MAX_PACKET_NUM = 255; // the max packet number
    private static final int INVALID_PACKET_NUM = -1; // invalid packet number

    private final EegDevice device;
    private int nextPackNum = INVALID_PACKET_NUM; // the next packet number wanted to received
    private final ISignalFilter eegFilter; // eeg filter
    private ExecutorService procService; // eeg data process Service

    public EegDataProcessor(EegDevice device) {
        if(device == null) {
            throw new NullPointerException("The device is null.");
        }

        this.device = device;
        eegFilter = new SignalPreFilter(device.getSampleRate());
    }

    public void reset() {
        eegFilter.design(device.getSampleRate());
    }

    public synchronized void start() {
        nextPackNum = 0;
        if(ExecutorUtil.isDead(procService)) {
            procService = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_Ecg_Process");
                }
            });

            ViseLog.e("The eeg data processor is started.");
        }
    }

    public synchronized void stop() {
        ViseLog.e("The eeg data processor is stopped.");

        ExecutorUtil.shutdownNowAndAwaitTerminate(procService);
    }

    public synchronized void processData(final byte[] data) {
        if(!ExecutorUtil.isDead(procService)) {
            procService.execute(new Runnable() {
                @Override
                public void run() {
                    int packageNum = UnsignedUtil.getUnsignedByte(data[0]);
                    if(packageNum == nextPackNum) { // good packet
                        int[] pack = resolveData(data, 1);
                        ViseLog.i("Packet No." + packageNum + ": " + Arrays.toString(pack));
                        nextPackNum = (nextPackNum == MAX_PACKET_NUM) ? 0 : nextPackNum+1;
                    } else if(nextPackNum != INVALID_PACKET_NUM){ // bad packet, force disconnect
                        ViseLog.e("The eeg data packet is lost.");
                        nextPackNum = INVALID_PACKET_NUM;
                        device.disconnect(false);
                    }
                    // invalid packet
                }
            });
        }
    }

    private int[] resolveData(byte[] data, int begin) {
        int[] pack = new int[(data.length-begin) / 3];
        for (int j = 0, i = begin; i < data.length; i=i+3, j++) {
            pack[j] = ByteUtil.getInt(new byte[]{0x00, data[i], data[i+1], data[i+2]});
            pack[j] >>= 8;
            int fData = (int) eegFilter.filter(pack[j]);
            device.showEegSignal(fData);
            device.recordEegSignal(fData);
        }
        return pack;
    }
}
