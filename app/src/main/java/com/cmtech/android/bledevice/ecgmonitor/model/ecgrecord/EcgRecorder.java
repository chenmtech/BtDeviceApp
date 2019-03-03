package com.cmtech.android.bledevice.ecgmonitor.model.ecgrecord;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_FILE_DIR;

/**
 * EcgRecorder: 心电记录器
 * Created by Chenm, 2018-12-27
 */

public class EcgRecorder {
    private EcgFile ecgFile; // 心电文件
    private long recordDataNum; // 记录的数据个数
    private int sampleRate = 125; // 采样频率
    private final List<IEcgAppendix> appendixList = new ArrayList<>(); // 当前信号的附加信息表
    private IEcgRecordObserver observer; // 心电记录观察者

    // 获取记录的秒数
    public int getRecordSecond() {
        return (int)(recordDataNum/sampleRate);
    }

    // 获取记录的秒数
    public long getRecordDataNum() {
        return recordDataNum;
    }

    public EcgRecorder() {

    }

    // 创建记录
    public synchronized void create(int sampleRate, int calibrationValue, EcgLeadType leadType, String macAddress) {
        // 已经创建过
        if(ecgFile != null) return;

        ecgFile = EcgFile.create(sampleRate, calibrationValue, macAddress, leadType);
        if(ecgFile != null) {
            appendixList.clear();
            recordDataNum = 0;
            this.sampleRate = sampleRate;
            notifyObserver(0);
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
                notifyObserver(getRecordSecond());
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
                    if (!appendixList.isEmpty()) {
                        ecgFile.addAppendix(appendixList);
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
        save();
    }

    // 添加一条附加信息
    public void addAppendix(IEcgAppendix appendix) {
        appendixList.add(appendix);
    }

    public void registerObserver(IEcgRecordObserver observer) {
        this.observer = observer;
    }

    private void notifyObserver(int second) {
        if(observer != null) {
            observer.updateRecordSecond(second);
        }
    }

    public void removeObserver() {
        observer = null;
    }



}
