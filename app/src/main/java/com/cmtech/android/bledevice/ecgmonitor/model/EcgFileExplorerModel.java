package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;

import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.vise.log.ViseLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final File ecgFileDir; // Ecg文件目录

    private final File[] allEcgFiles;

    private EcgFile selectFile; // 选中的EcgFile

    private EcgFilesManager filesManager; // 文件列表管理器

    private EcgHrRecorder hrRecorder;

    private OnEcgFileExploreListener listener; // 文件浏览监听器

    private final ExecutorService openFileService = Executors.newSingleThreadExecutor();

    private class OpenFileRunnable implements Runnable {
        private File file;

        OpenFileRunnable(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            EcgFile ecgFile = null;

            try {
                ecgFile = EcgFile.open(file.getCanonicalPath());

                filesManager.add(ecgFile);

                ViseLog.e(ecgFile.toString());
            } catch (IOException e) {
                ViseLog.e("To open ecg file is wrong." + file);
            } finally {
                if (ecgFile != null) {
                    try {
                        ecgFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public EcgFileExplorerModel(File ecgFileDir, OnEcgFileExploreListener listener) throws IOException{
        if(ecgFileDir == null) {
            throw new IOException();
        }

        if(!ecgFileDir.exists() && !ecgFileDir.mkdir()) {
            throw new IOException();
        }

        if(ecgFileDir.exists() && !ecgFileDir.isDirectory()) {
            throw new IOException();
        }

        this.ecgFileDir = ecgFileDir;

        allEcgFiles = BleDeviceUtil.listDirBmeFiles(ecgFileDir); // 列出所有bme文件

        filesManager = new EcgFilesManager(this);

        hrRecorder = new EcgHrRecorder(listener);

        this.listener = listener;

    }

    // 打开所有文件
    public void openAllFiles() {
        for(File file : allEcgFiles) {
            openFile(file);
        }
    }

    private void openFile(File file) {
        openFileService.submit(new OpenFileRunnable(file));
    }

    // 选中一个文件
    public void select(EcgFile ecgFile) {
        if(ecgFile == null) return;

        try {
            ecgFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        filesManager.select(ecgFile);
    }

    // 删除选中文件
    public void deleteSelectFile() {
        if(selectFile != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(MyApplication.getContext());
            builder.setTitle("删除Ecg信号");
            builder.setMessage("确定删除该Ecg信号吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    filesManager.delete(selectFile);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        }
    }

    // 从微信导入文件
    public void importFromWechat() {
        filesManager.importToFromWechat(ecgFileDir);
    }


    // 通过微信分享选中文件
    public void shareSelectFileThroughWechat() {
        filesManager.shareThroughWechat(selectFile);
    }

    // 保存留言信息
    public void saveAppendix() {
        if(selectFile != null) {
            try {
                selectFile.save();
            } catch (IOException e) {
                ViseLog.e("保存留言错误。");
            }
        }
    }

    public void updateHrInfo() {
        if(selectFile != null) {
            hrRecorder.setHrList(selectFile.getHrList());
            hrRecorder.updateHrInfo(10, 5);
        }
    }

    public void close() {
        if(selectFile != null) {
            try {
                selectFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        listener = null;

        openFileService.shutdownNow();
    }


    @Override
    public void onSelectFileChanged(final EcgFile ecgFile) {
        selectFile = ecgFile;

        if(listener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onSelectFileChanged(ecgFile);
                }
            });
        }
    }

    @Override
    public void onFileListChanged(final List<EcgFile> fileList) {
        if(listener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onFileListChanged(fileList);
                }
            });
        }
    }


}
