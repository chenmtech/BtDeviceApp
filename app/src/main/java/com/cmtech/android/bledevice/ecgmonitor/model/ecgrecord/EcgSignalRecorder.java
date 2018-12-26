package com.cmtech.android.bledevice.ecgmonitor.model.ecgrecord;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECGFILEDIR;

/**
 * EcgSignalRecorder: 心电信号记录器
 * Created by Chenm, 2018-12-27
 */

public class EcgSignalRecorder {
    private EcgFile ecgFile;
    private long recordDataNum;
    private IEcgRecordNumObserver observer;

    // 初始化EcgFile
    public boolean initialize(int sampleRate, int calibrationValue, EcgLeadType leadType, String macAddress) {
        if(ecgFile != null) return false;

        ecgFile = EcgFile.create(sampleRate, calibrationValue, macAddress, leadType);
        if(ecgFile != null) {
            //commentList.clear();
            recordDataNum = 0;
            notifyObserver(0);
            return true;
        }
        return false;
    }

    public void record(int ecgSignal) {
        try {
            ecgFile.writeData(ecgSignal);
            recordDataNum++;
            notifyObserver(recordDataNum);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 保存Ecg文件
    public void save(List<EcgComment> commentList) {
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

    public void registerObserver(IEcgRecordNumObserver observer) {
        this.observer = observer;
    }
    private void notifyObserver(long num) {
        if(observer != null) {
            observer.update(num);
        }
    }
    public void removeObserver() {
        observer = null;
    }
}
