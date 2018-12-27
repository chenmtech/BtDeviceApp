package com.cmtech.android.bledevice.ecgmonitor.model.ecgrecord;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECGFILEDIR;

/**
 * EcgSignalRecorder: 心电信号记录器
 * Created by Chenm, 2018-12-27
 */

public class EcgSignalRecorder {
    private EcgFile ecgFile;
    private long recordDataNum;
    private int sampleRate = 125;
    private final List<EcgComment> commentList = new ArrayList<>(); // 当前信号的留言表
    private IEcgRecordSecondObserver observer;

    // 初始化EcgFile
    public void initialize(int sampleRate, int calibrationValue, EcgLeadType leadType, String macAddress) {
        if(ecgFile != null) return;

        ecgFile = EcgFile.create(sampleRate, calibrationValue, macAddress, leadType);
        if(ecgFile != null) {
            commentList.clear();
            recordDataNum = 0;
            this.sampleRate = sampleRate;
            notifyObserver(0);
        } else {
            throw new IllegalStateException("创建心电文件失败");
        }
    }

    public int getRecordSecond() {
        return (int)(recordDataNum/sampleRate);
    }

    public void record(int ecgSignal) {
        try {
            ecgFile.writeData(ecgSignal);
            recordDataNum++;
            notifyObserver(getRecordSecond());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 保存Ecg文件
    public void save() {
        if (ecgFile != null) {
            try {
                if (ecgFile.getDataNum() <= 0) {     // 如果没有数据，删除文件
                    ecgFile.close();
                    FileUtil.deleteFile(ecgFile.getFile());
                } else {    // 如果有数据
                    if (!commentList.isEmpty()) {
                        ecgFile.addComments(commentList);
                    }
                    ecgFile.saveFileTail();
                    ecgFile.close();
                    ViseLog.e(ecgFile);
                    File toFile = FileUtil.getFile(ECGFILEDIR, ecgFile.getFile().getName());
                    // 将缓存区中的文件移动到ECGFILEDIR目录中
                    FileUtil.moveFile(ecgFile.getFile(), toFile);
                }
                ecgFile = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 添加没有时间定位的留言
    public void addComment(String comment) {
        long timeCreated = new Date().getTime();
        commentList.add(new EcgComment(UserAccountManager.getInstance().getUserAccount().getUserName(), timeCreated, comment));
    }

    // 添加有时间定位的留言
    public void addComment(int secondInEcg, String comment) {
        long timeCreated = new Date().getTime();
        commentList.add(new EcgComment(UserAccountManager.getInstance().getUserAccount().getUserName(), timeCreated, secondInEcg, comment));
    }

    public void registerObserver(IEcgRecordSecondObserver observer) {
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
