package com.cmtech.android.bledevice.ecgmonitor.model;


import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgLocatedComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.bmefile.BmeFileHead30;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * EcgFileReplayModel: 心电文件回放模型类
 * Created by bme on 2018/11/10.
 */

public class EcgFileReplayModel {
    private static final float DEFAULT_SECOND_PER_GRID = 0.04f;                 // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f;                      // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10;                       // 缺省每个栅格包含的像素个数

    private EcgFile ecgFile; // 播放的EcgFile
    private boolean updated = false; // 文件是否已更新
    private IEcgFileReplayObserver observer; // 文件播放观察者
    private final int totalSecond; // 信号总的秒数
    private long dataLocation = 0; // 记录当前播放的Ecg的秒数
    private long dataLocationWhenComment = -1; // 留言时间
    private boolean showSecondInComment = false; // 是否在留言中加入时间定位
    private final int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // 每小格的像素个数
    private final int xPixelPerData; // 横向分辨率
    private final float yValuePerPixel; // 纵向分辨率

    public boolean isShowSecondInComment() {
        return showSecondInComment;
    }
    public void setShowSecondInComment(boolean showSecondInComment) {
        this.showSecondInComment = showSecondInComment;
        if(showSecondInComment) {
            dataLocationWhenComment = dataLocation;
        }
        if(observer != null) {
            observer.updateShowSecondInComment(showSecondInComment, (int)(dataLocationWhenComment/ecgFile.getFs()));
        }
    }


    public int getPixelPerGrid() { return pixelPerGrid; }

    public int getxPixelPerData() { return xPixelPerData; }

    public float getyValuePerPixel() { return yValuePerPixel; }


    public long getDataLocation() { return dataLocation; }
    public int getCurrentSecond() {
        return (int)(dataLocation/ecgFile.getFs());
    }
    public void setDataLocation(long dataLocation) {
        this.dataLocation = dataLocation;
    }
    public boolean isUpdated() { return updated; }
    public int getTotalSecond() {
        return totalSecond;
    }

    public EcgFileReplayModel(String ecgFileName) throws IOException{
        ecgFile = EcgFile.open(ecgFileName);
        int sampleRate = ecgFile.getFs();
        totalSecond = ecgFile.getDataNum()/sampleRate;
        int value1mV = ((BmeFileHead30)ecgFile.getBmeFileHead()).getCalibrationValue();
        xPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_GRID * sampleRate));   // 计算横向分辨率
        yValuePerPixel = value1mV * DEFAULT_MV_PER_GRID / pixelPerGrid;                     // 计算纵向分辨率
    }

    public EcgFile getEcgFile() {
        return ecgFile;
    }

    public List<IEcgAppendix> getAppendixList() {
        return ecgFile.getAppendixList();
    }

    // 添加一个留言
    public void addComment(String comment) {
        String creator = UserAccountManager.getInstance().getUserAccount().getUserName();
        long createTime = new Date().getTime();
        if(showSecondInComment) {
            addAppendix(new EcgLocatedComment(creator, createTime, comment, dataLocationWhenComment));
            showSecondInComment = false;
            if(observer != null) {
                observer.updateShowSecondInComment(false, -1);
            }
        }
        else
            addAppendix(new EcgNormalComment(creator, createTime, comment));
    }

    private void addAppendix(IEcgAppendix appendix) {
        ecgFile.addAppendix(appendix);
        updated = true;
        updateAppendixList();
    }

    public void deleteAppendix(IEcgAppendix appendix) {
        ecgFile.deleteAppendix(appendix);
        updated = true;
        updateAppendixList();
    }


    public void close() {
        if(ecgFile != null) {
            if(updated) {
                ecgFile.saveFileTail();
            }
            try {
                ecgFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 登记心电回放观察者
    public void registerEcgFileReplayObserver(IEcgFileReplayObserver observer) {
        this.observer = observer;
    }

    // 删除心电回放观察者
    public void removeEcgFileReplayObserver() {
        observer = null;
    }

    private void updateAppendixList() {
        if(observer != null) {
            observer.updateAppendixList();
        }
    }
}
