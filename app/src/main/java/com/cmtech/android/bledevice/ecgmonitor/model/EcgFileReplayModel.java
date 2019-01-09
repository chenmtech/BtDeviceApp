package com.cmtech.android.bledevice.ecgmonitor.model;


import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.bmefile.BmeFileHead30;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class EcgFileReplayModel {
    private static final float DEFAULT_SECOND_PER_GRID = 0.04f;                 // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f;                      // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10;                       // 缺省每个栅格包含的像素个数

    private EcgFile ecgFile;            // 播放的EcgFile

    private boolean updated = false;            // 文件是否已更新
    public boolean isUpdated() { return updated; }

    // 文件播放观察者
    private IEcgFileReplayObserver observer;

    private final int totalSecond;                   // 信号总的秒数
    public int getTotalSecond() {
        return totalSecond;
    }

    private int currentSecond = 0;                 // 记录当前播放的Ecg的秒数
    public int getCurrentSecond() { return currentSecond; }
    public void setCurrentSecond(int currentSecond) {
        this.currentSecond = currentSecond;
    }

    private int secondWhenComment = -1;        // 留言时间

    private boolean showSecondInComment = false;       // 是否在留言中加入时间定位
    public boolean isShowSecondInComment() {
        return showSecondInComment;
    }
    public void setShowSecondInComment(boolean showSecondInComment) {
        this.showSecondInComment = showSecondInComment;
        if(showSecondInComment) {
            secondWhenComment = currentSecond;
        }
        if(observer != null) {
            observer.updateShowSecondInComment(showSecondInComment, secondWhenComment);
        }
    }

    private final int pixelPerGrid = DEFAULT_PIXEL_PER_GRID;                   // 每小格的像素个数
    public int getPixelPerGrid() { return pixelPerGrid; }
    private final int xPixelPerData;     // 横向分辨率
    public int getxPixelPerData() { return xPixelPerData; }
    private final float yValuePerPixel;                      // 纵向分辨率
    public float getyValuePerPixel() { return yValuePerPixel; }

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

    public List<EcgComment> getCommentList() {
        return ecgFile.getCommentList();
    }

    // 添加一个留言
    public void addComment(String comment) {
        if(showSecondInComment) {
            addComment(secondWhenComment, comment);
            showSecondInComment = false;
            if(observer != null) {
                observer.updateShowSecondInComment(false, -1);
            }
        }
        else
            addComment(-1, comment);
    }

    // 添加一个有时间定位的留言
    private void addComment(int secondInEcg, String comment) {
        String commentator = UserAccountManager.getInstance().getUserAccount().getUserName();
        long timeCreated = new Date().getTime();
        ecgFile.addComment(new EcgComment(commentator, timeCreated, secondInEcg, comment));
        updated = true;
        updateCommentList();
    }

    public void deleteComment(EcgComment comment) {
        ecgFile.deleteComment(comment);
        updated = true;
        updateCommentList();
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
    public void removeEcgFileObserver() {
        observer = null;
    }

    private void updateCommentList() {
        if(observer != null) {
            observer.updateCommentList();
        }
    }
}
