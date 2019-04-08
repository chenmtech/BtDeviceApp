package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;

import java.io.IOException;

/**
 * EcgSignalRecorder: 心电信号记录仪，包含心电信号的文件记录，以及留言信息的管理
 * Created by Chenm, 2018-12-27
 */

public class EcgSignalRecorder {
    public interface IEcgSignalSecNumUpdatedListener {
        void onUpdateSignalSecNum(int second); // 更新心电信号记录的秒数
    }

    private final EcgFile ecgFile;

    private int recordDataNum = 0;

    private int sampleRate; // 采样频率

    private boolean isRecord = false;

    private final EcgNormalComment comment; // 当前信号的留言

    private IEcgSignalSecNumUpdatedListener listener; // 心电信号记录秒数更新监听器

    EcgSignalRecorder(int sampleRate, EcgFile ecgFile, IEcgSignalSecNumUpdatedListener listener) {
        this.sampleRate = sampleRate;

        this.ecgFile = ecgFile;

        this.listener = listener;

        comment = EcgNormalComment.createDefaultComment();
    }

    // 获取记录的秒数
    int getSecond() {
        return recordDataNum /sampleRate;
    }

    // 获取记录的数据个数
    long getDataNum() {
        return recordDataNum;
    }

    EcgNormalComment getComment() {
        return comment;
    }

    boolean isRecord() {
        return isRecord;
    }

    void setRecord(boolean record) {
        isRecord = record;
    }

    // 记录心电信号
    synchronized void record(int ecgSignal) throws IOException{
        if(isRecord) {
            ecgFile.writeData(ecgSignal);
            recordDataNum++;
            if (listener != null) listener.onUpdateSignalSecNum(getSecond());
        }
    }

    public synchronized void close() {
        isRecord = false;
        listener = null;
    }

    // 添加留言的内容
    void addCommentContent(String content) {
        if(isRecord)
            comment.appendContent(content);
    }
}
