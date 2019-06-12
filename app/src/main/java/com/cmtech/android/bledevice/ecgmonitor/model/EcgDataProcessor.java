package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.LinkedBlockingQueue;

public class EcgDataProcessor {
    private final LinkedBlockingQueue<Integer> dataBuff = new LinkedBlockingQueue<>();	//数据缓存

    private final LinkedBlockingQueue<byte[]> shortBuff = new LinkedBlockingQueue<byte[]>();

    private EcgCalibrateDataProcessor calibrateDataProcessor; // 标定数据处理器

    private EcgSignalProcessor signalProcessor; // 心电信号处理器

    EcgDataProcessor() {
    }

    void setCalibrateDataProcessor(EcgCalibrateDataProcessor calibrateDataProcessor) {
        this.calibrateDataProcessor = calibrateDataProcessor;
    }

    void setSignalProcessor(EcgSignalProcessor signalProcessor) {
        this.signalProcessor = signalProcessor;
    }

    // 解析数据包
    void resolveDataPacket(byte[] data) throws InterruptedException{
        shortBuff.put(data);
    }

    synchronized void processCalibrateData() throws InterruptedException{
        // 单片机发过来的是LITTLE_ENDIAN的short数据
        byte[] data = shortBuff.take();

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        // 单片机发过来的int是两个字节的short
        for (int i = 0; i < data.length / 2; i++) {
            calibrateDataProcessor.process((int)buffer.getShort());
        }
    }

    synchronized void processEcgSignalData() throws InterruptedException {
        // 单片机发过来的是LITTLE_ENDIAN的short数据
        byte[] data = shortBuff.take();

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        // 单片机发过来的int是两个字节的short
        for (int i = 0; i < data.length / 2; i++) {
            signalProcessor.process((int)buffer.getShort());
        }
    }

    void clearData() {
        shortBuff.clear();
    }

}
