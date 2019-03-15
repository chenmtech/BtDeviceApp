package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.bmefile.BmeFileHead30;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EcgFileExplorerModel: 心电文件浏览模型类
 * Created by bme on 2018/11/10.
 */

public class EcgFileExplorerModel {
    private final File fileDir; // 文件目录
    private EcgFile selectFile; // 播放的EcgFile
    private EcgFileListOp fileOp;
    private IEcgFileExplorerObserver observer;

    private static final float DEFAULT_SECOND_PER_GRID = 0.04f;                 // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f;                      // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10;                       // 缺省每个栅格包含的像素个数

    private int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // 每小格的像素个数
    private int xPixelPerData; // 横向分辨率
    private float yValuePerPixel; // 纵向分辨率
    private int totalSecond; // 信号总的秒数
    private long dataLocation = 0; // 当前播放的数据位置

    public int getPixelPerGrid() { return pixelPerGrid; }
    public int getxPixelPerData() { return xPixelPerData; }
    public float getyValuePerPixel() { return yValuePerPixel; }
    public long getDataLocation() { return dataLocation; }
    public void setDataLocation(long dataLocation) {
        this.dataLocation = dataLocation;
    }
    public int getCurrentSecond() {
        return (int)(dataLocation/ selectFile.getFs());
    }
    public int getTotalSecond() {
        return totalSecond;
    }
    public EcgFile getSelectFile() {
        return selectFile;
    }
    public int getSampleRate() {
        return selectFile.getFs();
    }

    public int getSelectIndex() { return fileOp.getSelectIndex(); }

    public List<EcgFile> getFileList() { return fileOp.getFileList(); }

    public IEcgFileExplorerObserver getObserver() {
        return observer;
    }

    public EcgFileExplorerModel(File fileDir) throws IOException{
        if(fileDir == null || !fileDir.isDirectory()) {
            throw new IllegalArgumentException();
        }

        this.fileDir = fileDir;

        if(!fileDir.exists() && !fileDir.mkdir()) {
            throw new IOException("磁盘空间不足");
        }

        fileOp = new EcgFileListOp(fileDir, this);
    }

    public List<EcgAppendix> getSelectFileAppendixList() {
        if(selectFile == null)
            return new ArrayList<>();
        else
            return selectFile.getAppendixList();
    }

    public int getSelectFileSampleRate() {
        if(selectFile == null){
            return -1;
        } else {
            return selectFile.getFs();
        }
    }

    // 选中一个文件
    public void select(int index) {
        fileOp.select(index);
    }

    // 删除所选文件
    public void deleteSelectFile() {
        fileOp.deleteSelectFile();
    }

    // 从微信导入文件
    public void importFromWechat() {
        fileOp.importToFromWechat(fileDir);
    }

    // 用微信分享BME文件
    public void shareSelectFileThroughWechat() {
        fileOp.shareSelectFileThroughWechat();
    }

    public void changeSelectFile(EcgFile ecgFile) {
        selectFile = ecgFile;
        initReplayPara(selectFile);
    }

    private void initReplayPara(EcgFile ecgFile) {
        int sampleRate = ecgFile.getFs();
        totalSecond = ecgFile.getDataNum()/sampleRate;
        int value1mV = ((BmeFileHead30)ecgFile.getBmeFileHead()).getCalibrationValue();
        xPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_GRID * sampleRate));   // 计算横向分辨率
        yValuePerPixel = value1mV * DEFAULT_MV_PER_GRID / pixelPerGrid; // 计算纵向分辨率
        dataLocation = 0;
    }

    public void saveAppendix(EcgAppendix ecgAppendix) {
        if(selectFile != null && selectFile.getAppendixList().contains(ecgAppendix)) {
            int index = selectFile.getAppendixList().indexOf(ecgAppendix);
            selectFile.getAppendixList().get(index).setContent(ecgAppendix.getContent());
        }
    }

    // 登记心电文件浏览器观察者
    public void registerEcgFileExplorerObserver(IEcgFileExplorerObserver observer) {
        this.observer = observer;
    }

    // 删除心电文件浏览器观察者
    public void removeEcgFileExplorerObserver() {
        observer = null;
    }


}
