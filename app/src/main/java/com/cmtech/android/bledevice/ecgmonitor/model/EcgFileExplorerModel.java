package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.vise.log.ViseLog;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
  *
  * ClassName:      EcgFileExplorerModel
  * Description:    Ecg文件浏览器模型类
  * Author:         chenm
  * CreateDate:     2018/11/10 下午4:09
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午4:09
  * UpdateRemark:   用类图优化代码
  * Version:        1.0
 */

public class EcgFileExplorerModel implements EcgFilesManager.OnEcgFilesChangeListener {

    private class OpenFileRunnable implements Runnable {
        private final File file;

        OpenFileRunnable(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            try {
                EcgFile ecgFile = EcgFile.open(file.getCanonicalPath());

                filesManager.add(ecgFile);

                ViseLog.e(ecgFile.toString());
            } catch (IOException e) {
                ViseLog.e("To open ecg file is wrong." + file);
            }
        }
    }

    private final EcgFilesManager filesManager; // 文件列表管理器

    private final OnEcgFileExploreListener listener; // 文件浏览监听器

    private final ExecutorService openFileService = Executors.newSingleThreadExecutor();

    private EcgFileExplorerModel(File ecgFileDir, OnEcgFileExploreListener listener) throws IOException{
        if(ecgFileDir == null) {
            throw new IOException("The ecg file dir is null");
        }

        if(!ecgFileDir.exists() && !ecgFileDir.mkdir()) {
            throw new IOException("The ecg file dir doesn't exist.");
        }

        if(ecgFileDir.exists() && !ecgFileDir.isDirectory()) {
            throw new IOException("The ecg file dir is invalid.");
        }

        filesManager = new EcgFilesManager(ecgFileDir);

        this.listener = listener;
    }

    public static EcgFileExplorerModel newInstance(File ecgFileDir, OnEcgFileExploreListener listener) throws IOException{
        EcgFileExplorerModel model = new EcgFileExplorerModel(ecgFileDir, listener);

        model.filesManager.setListener(model);

        return model;
    }

    // 打开所有文件
    public void openAllFiles() {
        for(File file : filesManager.getFileList()) {
            openFile(file);
        }
    }

    private void openFile(File file) {
        openFileService.execute(new OpenFileRunnable(file));
    }

    // 选中一个文件
    public void selectFile(EcgFile ecgFile) {
        if(ecgFile == null) return;

        filesManager.select(ecgFile);
    }

    // 删除选中文件
    public void deleteSelectFile(Context context) {
        filesManager.deleteSelectFile(context);
    }

    // 从微信导入文件
    public void importFromWechat() {
        filesManager.importFromWechat();
    }

    // 用微信分享一个文件
    public void shareSelectFileThroughWechat(final Context context) {
        filesManager.shareSelectFileThroughWechat(context);
    }

    // 保存留言信息
    public void saveSelectFileComment() {
        try {
            filesManager.saveSelectFileComment();
        } catch (IOException e) {
            ViseLog.e("保存留言错误。");
        }

    }

    public void getSelectFileHrInfo() {
        if(listener != null)
            listener.onEcgHrInfoUpdated(filesManager.getSelectFileHrInfo());

    }

    public void close() {
        try {
            openFileService.shutdownNow();

            openFileService.awaitTermination(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {

        }

        filesManager.close();
    }

    @Override
    public void onFileListChanged(final List<EcgFile> fileList) {
        if(listener != null)
            listener.onFileListChanged(fileList);
    }

    @Override
    public void onSelectFileChanged(final EcgFile ecgFile) {
        if(listener != null)
            listener.onSelectFileChanged(ecgFile);

    }

}
