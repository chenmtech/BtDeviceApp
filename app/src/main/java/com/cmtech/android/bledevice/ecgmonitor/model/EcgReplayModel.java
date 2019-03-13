package com.cmtech.android.bledevice.ecgmonitor.model;


import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.bmefile.BmeFileHead30;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * EcgReplayModel: 心电文件回放模型类
 * Created by bme on 2018/11/10.
 */

public class EcgReplayModel {
    private static final float DEFAULT_SECOND_PER_GRID = 0.04f;                 // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f;                      // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10;                       // 缺省每个栅格包含的像素个数

    private final int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // 每小格的像素个数
    private final int xPixelPerData; // 横向分辨率
    private final float yValuePerPixel; // 纵向分辨率

    private final EcgFile ecgFile; // 播放的EcgFile
    private boolean updated = false; // 文件是否已更新
    private IEcgReplayObserver observer; // 文件播放观察者
    private final int totalSecond; // 信号总的秒数
    private long dataLocation = 0; // 当前播放的数据位置
    private long dataLocationWhenAppendix = -1; // 添加附加信息时的数据位置
    private boolean showAppendixTime = false; // 是否在附加信息中加入时间


    public EcgReplayModel(String ecgFileName) throws IOException{
        ecgFile = EcgFile.open(ecgFileName);
        int sampleRate = ecgFile.getFs();
        totalSecond = ecgFile.getDataNum()/sampleRate;
        int value1mV = ((BmeFileHead30)ecgFile.getBmeFileHead()).getCalibrationValue();
        xPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_GRID * sampleRate));   // 计算横向分辨率
        yValuePerPixel = value1mV * DEFAULT_MV_PER_GRID / pixelPerGrid; // 计算纵向分辨率

        User account = AccountManager.getInstance().getAccount();
        boolean found = false;
        for(EcgAppendix appendix : ecgFile.getAppendixList()) {
            if(appendix.getCreator().equals(account)) {
                found = true;
                break;
            }
        }
        if(!found) {
            ecgFile.addAppendix(EcgAppendix.createDefaultAppendix());
        }
    }

    /*public boolean isShowAppendixTime() {
        return showAppendixTime;
    }
    public void setShowAppendixTime(boolean showAppendixTime) {
        this.showAppendixTime = showAppendixTime;
        if(showAppendixTime) {
            dataLocationWhenAppendix = dataLocation;
        }
        if(observer != null) {
            observer.updateIsShowTimeInAppendix(showAppendixTime, (int)(dataLocationWhenAppendix /ecgFile.getFs()));
        }
    }*/
    public int getPixelPerGrid() { return pixelPerGrid; }
    public int getxPixelPerData() { return xPixelPerData; }
    public float getyValuePerPixel() { return yValuePerPixel; }
    public long getDataLocation() { return dataLocation; }
    public void setDataLocation(long dataLocation) {
        this.dataLocation = dataLocation;
    }
    public int getCurrentSecond() {
        return (int)(dataLocation/ecgFile.getFs());
    }
    public int getTotalSecond() {
        return totalSecond;
    }
    public boolean isUpdated() { return updated; }
    public EcgFile getEcgFile() {
        return ecgFile;
    }
    public int getSampleRate() {
        return ecgFile.getFs();
    }
    public List<EcgAppendix> getAppendixList() {
        return ecgFile.getAppendixList();
    }

    // 添加一条附言
    public void addAppendix(String content) {
        EcgAppendix appendix = createAppendix(content);
        addAppendix(appendix);
    }

    // 保存一条附言
    public void saveAppendix(EcgAppendix appendix) {
        if(ecgFile.getAppendixList().contains(appendix)) {
            updated = true;
        }
    }

    private EcgAppendix createAppendix(String content) {
        User creator = AccountManager.getInstance().getAccount();
        long createTime = new Date().getTime();
        return new EcgAppendix(creator, createTime, content);
    }

    private void addAppendix(EcgAppendix appendix) {
        ecgFile.addAppendix(appendix);
        updated = true;
        if(observer != null) {
            observer.updateAppendixList();
        }
    }

    public void deleteAppendix(EcgAppendix appendix) {
        ecgFile.deleteAppendix(appendix);
        updated = true;
        if(observer != null) {
            observer.updateAppendixList();
        }
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
    public void registerEcgFileReplayObserver(IEcgReplayObserver observer) {
        this.observer = observer;
    }

    // 删除心电回放观察者
    public void removeEcgFileReplayObserver() {
        observer = null;
    }

}
