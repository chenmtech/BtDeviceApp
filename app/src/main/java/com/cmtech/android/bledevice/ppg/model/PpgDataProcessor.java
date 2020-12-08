package com.cmtech.android.bledevice.ppg.model;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledeviceapp.dataproc.ISignalFilter;
import com.cmtech.android.bledeviceapp.dataproc.SignalPreFilter;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;
import com.vise.log.ViseLog;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
  *
  * ClassName:      EegDataProcessor
  * Description:    eeg signal processor, including resolving the eeg data packet and filtering the data
  * Author:         chenm
  * CreateDate:     2020-06-11 05:17
  * UpdateUser:     chenm
  * UpdateDate:     2020-06-11 05:17
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class PpgDataProcessor {
    private static final int MAX_PACKET_NUM = 255; // the max packet number
    private static final int INVALID_PACKET_NUM = -1; // invalid packet number

    private final PpgDevice device;
    private int nextPackNum = INVALID_PACKET_NUM; // the next packet number wanted to received
    private final ISignalFilter ppgFilter; // eeg filter
    private ExecutorService procService; // eeg data process Service

    public PpgDataProcessor(PpgDevice device) {
        if(device == null) {
            throw new NullPointerException("The device is null.");
        }

        this.device = device;
        ppgFilter = new SignalPreFilter(device.getSampleRate());
    }

    public void reset() {
        ppgFilter.design(device.getSampleRate());
    }

    public synchronized void start() {
        nextPackNum = 0;
        if(ExecutorUtil.isDead(procService)) {
            procService = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_Ppg_Process");
                }
            });

            ViseLog.e("The ppg data processor is started.");
        }
    }

    public synchronized void stop() {
        ViseLog.e("The ppg data processor is stopped.");

        ExecutorUtil.shutdownNowAndAwaitTerminate(procService);
    }

    public synchronized void processData(final byte[] data) {
        if(!ExecutorUtil.isDead(procService)) {
            procService.execute(new Runnable() {
                @Override
                public void run() {
                    int packageNum = UnsignedUtil.getUnsignedByte(data[0]);
                    if(packageNum == nextPackNum) { // good packet
                        int[] pack = parseAndProcessDataPacket(data, 1);
                        if(nextPackNum == MAX_PACKET_NUM)
                            nextPackNum = 0;
                        else
                            nextPackNum++;
                        ViseLog.i("Packet No." + packageNum + ": " + Arrays.toString(pack));
                    } else if(nextPackNum != INVALID_PACKET_NUM){ // bad packet, force disconnect
                        ViseLog.e("The eeg data packet is lost. Disconnect device.");
                        nextPackNum = INVALID_PACKET_NUM;
                        device.disconnect(false);
                    }
                }
            });
        }
    }

    private int[] parseAndProcessDataPacket(byte[] data, int begin) {
        int[] pack = new int[(data.length-begin) / 3];
        for (int i = begin, j = 0; i < data.length; i=i+3, j++) {
            pack[j] = ByteUtil.getInt(new byte[]{0x00, data[i], data[i+1], data[i+2]});
            pack[j] >>= 8;
            int fData = (int) ppgFilter.filter(pack[j]);
            device.showPpgSignal(fData);
            device.recordPpgSignal(fData);
        }
        return pack;
    }
}
