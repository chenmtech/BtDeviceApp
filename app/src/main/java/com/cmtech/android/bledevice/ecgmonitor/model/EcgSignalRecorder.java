package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;

import java.io.IOException;

/**
 * EcgSignalRecorder: 心电信号记录仪，包含心电信号的文件记录，以及留言信息的管理
 * Created by Chenm, 2018-12-27
 */

public class EcgSignalRecorder {
    private final EcgMonitorDevice device;
    private final EcgRecord ecgRecord; // ECG记录
    private final EcgNormalComment comment; // 当前信号的一般性留言
    private final int sampleRate; // 采样频率
    private int dataNum = 0; // 记录的数据个数
    private boolean isRecording = false; // 是否记录

    public EcgSignalRecorder(EcgMonitorDevice device) {
        if(device == null) {
            throw new NullPointerException("The device is null.");
        }
        if(device.getEcgRecord() == null) {
            throw new NullPointerException("The ecg record is null.");
        }

        this.device = device;
        this.ecgRecord = device.getEcgRecord();
        comment = EcgNormalComment.createDefaultComment();

        this.sampleRate = device.getSampleRate();
    }

    // 获取记录的秒数
    public int getSecond() {
        return dataNum /sampleRate;
    }
    // 获取记录的数据个数
    public long getDataNum() {
        return dataNum;
    }
    public EcgNormalComment getComment() {
        return comment;
    }
    public boolean isRecording() {
        return isRecording;
    }
    public synchronized void setRecording(boolean recording){
        isRecording = recording;
        if(isRecording) {
            try {
                ecgRecord.openSigFile();
                ecgRecord.writeData(device.getWaveData1mV());
                dataNum += device.getWaveData1mV().length;
                device.updateRecordSecond(getSecond());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

   // 记录心电信号
    public synchronized void record(int ecgSignal) throws IOException{
        if(isRecording) {
            ecgRecord.writeData(ecgSignal);
            dataNum++;
            device.updateRecordSecond(getSecond());
        }
    }

    // 添加留言的内容
    public void addCommentContent(String content) {
        if(isRecording)
            comment.appendContent(content);
    }
}
