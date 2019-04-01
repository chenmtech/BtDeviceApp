package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_FILE_DIR;

/**
 * EcgSignalRecorder: 心电记录器
 * Created by Chenm, 2018-12-27
 */

public class EcgSignalRecorder1 {
    public interface IEcgRecordSecondUpdatedListener {
        void onEcgRecordSecondUpdated(int second); // 更新记录的秒数
    }

    private EcgFile ecgFile; // 心电文件

    private long recordDataNum; // 记录的数据个数

    private int sampleRate = 125; // 采样频率

    private EcgNormalComment appendix; // 当前信号的留言

    private IEcgRecordSecondUpdatedListener listener; // 心电记录秒数更新监听器

    // 获取记录的秒数
    public int getRecordSecond() {
        return (int)(recordDataNum/sampleRate);
    }

    // 获取记录的秒数
    public long getRecordDataNum() {
        return recordDataNum;
    }

    public EcgSignalRecorder1() {

    }

    public void setHrList(List<Integer> hrList) {
        if(ecgFile != null)
            ecgFile.setHrList(hrList);
    }

    // 创建记录
    public synchronized void create(int sampleRate, int calibrationValue, EcgLeadType leadType, String macAddress) {
        // 已经创建过
        if(ecgFile != null) return;

        ecgFile = EcgFile.create(sampleRate, calibrationValue, macAddress, leadType);
        if(ecgFile != null) {
            appendix = EcgNormalComment.createDefaultComment();
            ViseLog.e(appendix.toString());
            recordDataNum = 0;
            this.sampleRate = sampleRate;
            if(listener != null) listener.onEcgRecordSecondUpdated(0);
        } else {
            throw new IllegalStateException("创建心电记录失败");
        }
    }

    // 记录心电信号
    public synchronized void record(int ecgSignal) {
        try {
            if(ecgFile != null) {
                ecgFile.writeData(ecgSignal);
                recordDataNum++;
                if(listener != null) listener.onEcgRecordSecondUpdated(getRecordSecond());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 保存记录
    public synchronized void save() {
        if (ecgFile != null) {
            try {
                if (ecgFile.getDataNum() <= 0) {     // 如果没有数据，删除文件
                    ecgFile.close();
                    FileUtil.deleteFile(ecgFile.getFile());
                } else {    // 如果有数据
                    if (appendix != null) {
                        ecgFile.addComment(appendix);
                    }

                    ecgFile.saveFileTail();
                    ecgFile.close();
                    ViseLog.e(ecgFile);
                    File toFile = FileUtil.getFile(ECG_FILE_DIR, ecgFile.getFile().getName());
                    // 将缓存区中的文件移动到ECGFILEDIR目录中
                    FileUtil.moveFile(ecgFile.getFile(), toFile);
                }
                ecgFile = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 关闭
    public void close() {
        removeEcgRecordSecondUpdatedListener();
        save();
    }

    // 添加留言的内容
    public void addAppendixContent(String content) {
        appendix.appendContent(content);
    }

    public void setEcgRecordSecondUpdatedListener(IEcgRecordSecondUpdatedListener listener) {
        this.listener = listener;
    }

    private void removeEcgRecordSecondUpdatedListener() {
        listener = null;
    }
}
