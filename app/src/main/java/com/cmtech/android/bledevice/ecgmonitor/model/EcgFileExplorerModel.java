package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * EcgFileExplorerModel: 心电文件浏览模型类
 * Created by bme on 2018/11/10.
 */

public class EcgFileExplorerModel {
    private final File fileDir; // 文件目录
    private EcgFileListOp fileOp;
    private EcgFileAppendixOp appendixOp;
    private EcgFileReplayOp replayOp;

    public int getSelectIndex() { return fileOp.getSelectIndex(); }

    public List<EcgFile> getFileList() { return fileOp.getFileList(); }

    public EcgFileExplorerModel(File fileDir) throws IOException{
        if(fileDir == null || !fileDir.isDirectory()) {
            throw new IllegalArgumentException();
        }

        this.fileDir = fileDir;

        if(!fileDir.exists() && !fileDir.mkdir()) {
            throw new IOException("磁盘空间不足");
        }

        fileOp = new EcgFileListOp(fileDir);
        appendixOp = new EcgFileAppendixOp();
    }

    public List<EcgAppendix> getSelectFileAppendixList() {
        return appendixOp.getAppendixList();
    }

    public int getSelectFileSampleRate() {
        return fileOp.getSelectFileSampleRate();
    }

    // 选中一个文件
    public void select(int index) {
        EcgFile selectFile = fileOp.select(index);
        if(selectFile != null) {
            appendixOp.setAppendixList(selectFile.getAppendixList());
        }
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

    // 登记心电文件浏览器观察者
    public void registerEcgFileExplorerObserver(IEcgFileExplorerObserver observer) {
        fileOp.registerEcgFileListObserver(observer);
    }

    // 删除心电文件浏览器观察者
    public void removeEcgFileExplorerObserver() {
        fileOp.removeEcgFileListObserver();
    }


}
