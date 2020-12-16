package com.cmtech.android.bledevice.hrm.model;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledeviceapp.dataproc.ISignalFilter;
import com.cmtech.android.bledeviceapp.dataproc.EcgSignalPreFilter;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;
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
  * UpdateDate:     2020-03-06 05:17
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgDataPacketProcessor {
    private static final int MAX_PACKET_NUM = 255; // the max packet number
    private static final int INVALID_PACKET_NUM = -1; // invalid packet number

    private final HrmDevice device;
    private int nextPackNum = INVALID_PACKET_NUM; // the next packet number wanted to received
    private final ISignalFilter preFilter; // ecg filter
    private ExecutorService procService; // ecg data process Service

    public EcgDataPacketProcessor(HrmDevice device) {
        if(device == null) {
            throw new NullPointerException("The device is null.");
        }

        this.device = device;
        preFilter = new EcgSignalPreFilter(device.getSampleRate());
    }

    public void reset() {
        preFilter.design(device.getSampleRate());
    }

    public synchronized void start() {
        if(ExecutorUtil.isDead(procService)) {
            nextPackNum = 0;
            procService = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_Ecg_Process");
                }
            });

            ViseLog.e("The ecg data processor is started.");
        } else {
            throw new IllegalStateException("The ecg data processor's executor is not stopped and can't be started.");
        }
    }

    public synchronized void stop() {
        ViseLog.e("The ecg data processor is stopped.");

        ExecutorUtil.shutdownNowAndAwaitTerminate(procService);
    }

    public synchronized void takePacket(final byte[] packet) {
        if(!ExecutorUtil.isDead(procService)) {
            procService.execute(new Runnable() {
                @Override
                public void run() {
                    processPacket(packet);
                }
            });
        }
    }

    private void processPacket(byte[] packet) {
        int packageNum = UnsignedUtil.getUnsignedByte(packet[0]);
        if (packageNum == nextPackNum) { // good packet
            int[] pack = resolvePacket(packet, 1);
            ViseLog.i("Packet No." + packageNum + ": " + Arrays.toString(pack));
            nextPackNum = (nextPackNum == MAX_PACKET_NUM) ? 0 : nextPackNum + 1;
        } else if (nextPackNum != INVALID_PACKET_NUM) { // bad packet, force disconnect
            ViseLog.e("The ecg data packet is lost.");
            nextPackNum = INVALID_PACKET_NUM;
            device.disconnect(false);
        }
        // invalid packet
    }

    private int[] resolvePacket(byte[] packet, int begin) {
        int[] pack = new int[(packet.length-begin) / 2];
        int j = 0;
        for (int i = begin; i < packet.length; i=i+2, j++) {
            pack[j] = (short) ((0xff & packet[i]) | (0xff00 & (packet[i+1] << 8)));
            int fData = (int) preFilter.filter(pack[j]);
            device.showEcgSignal(fData);
            device.recordEcgSignal(fData);
        }
        return pack;
    }
}
