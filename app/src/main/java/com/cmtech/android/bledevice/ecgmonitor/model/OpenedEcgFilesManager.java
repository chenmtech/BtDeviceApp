package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStatisticsInfo;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.R;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

import static cn.sharesdk.framework.Platform.SHARE_FILE;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor.HR_FILTER_SECOND;

/**
  *
  * ClassName:      OpenedEcgFilesManager
  * Description:    打开的Ecg文件管理器
  * Author:         chenm
  * CreateDate:     2019/4/12 上午8:03
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 上午8:03
  * UpdateRemark:   画了类图，根据类图优化了接口函数
  * Version:        1.0
 */

public class OpenedEcgFilesManager {
    private final List<EcgFile> openedFileList = new ArrayList<>(); // 打开的心电文件列表
    private final List<EcgFile> unmodifiedFileList = Collections.unmodifiableList(openedFileList);
    private final OnOpenedEcgFilesListener listener; // ECG文件目录监听器
    private volatile EcgFile selectedFile; // 选中的EcgFile
    private final int fileOrder;

    public interface OnOpenedEcgFilesListener {
        void onFileSelected(EcgFile ecgFile); // 文件被选中
        void onFileInserted(EcgFile ecgFile);
        void onFileListChanged(List<EcgFile> fileList); // 文件列表改变
    }

    OpenedEcgFilesManager(int fileOrder, OnOpenedEcgFilesListener listener) {
        this.fileOrder = fileOrder;
        this.listener = listener;
    }

    // 打开文件
    synchronized void openFile(File file) throws IOException{
        boolean contain = false;
        for(EcgFile ecgFile : openedFileList) {
            if(ecgFile.getFile().getName().equalsIgnoreCase(file.getName())) {
                contain = true;
                break;
            }
        }

        if(!contain) {
            EcgFile ecgFile = EcgFile.open(file.getCanonicalPath());
            openedFileList.add(ecgFile);
            //notifyFileListChanged();
            if(listener != null)
                listener.onFileInserted(ecgFile);
        }
    }

    // 选中文件
    synchronized void select(EcgFile file) {
        if(file == null) {
            selectedFile = null;
            notifySelectedFileChanged();
        } else if(openedFileList.contains(file)) {
            selectedFile = file;
            notifySelectedFileChanged();
        }
    }

    // 删除选中的文件
    synchronized void deleteSelectedFile(final Context context) {
        if(selectedFile != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("删除心电记录").setMessage("确定删除该心电记录吗？");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doDeleteSelectFile();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }
    }

    // 删除文件
    private synchronized void doDeleteSelectFile() {
        try {
            if(selectedFile != null) {
                FileUtil.deleteFile(selectedFile.getFile());
                if(openedFileList.remove(selectedFile)) {
                    notifyFileListChanged();
                }
                select(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized void saveSelectedFileComment() throws IOException{
        if(selectedFile != null) {
            selectedFile.saveFileTail();
        }
    }

    synchronized EcgHrStatisticsInfo getSelectedFileHrStatisticsInfo() {
        if(selectedFile != null) {
            return new EcgHrStatisticsInfo(selectedFile.getHrList(), HR_FILTER_SECOND);
        }
        return null;
    }

    // 通过微信分享选中文件
    synchronized void shareSelectedFileThroughWechat(final Context context) {
        if(selectedFile == null) return;
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        String fileShortName = selectedFile.getFile().getName();
        //sp.setTitle("分享文件");
        //String time = DateTimeUtil.timeToShortString(new Date().getTime());
        //sp.setTitle("心电记录by " + UserManager.getInstance().getUser().getName() + " " + time);
        sp.setTitle(fileShortName);
        sp.setComment("hi");
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_kang);
        sp.setImageData(bmp);
        sp.setFilePath(selectedFile.getFileName());
        Platform platform = ShareSDK.getPlatform(Wechat.NAME);
        platform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            }
            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Toast.makeText(context, "分享错误", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancel(Platform platform, int i) {
                Toast.makeText(context, "分享被取消", Toast.LENGTH_SHORT).show();
            }
        });
        platform.share(sp);
    }

    // 关闭管理器
    synchronized void close() {
        select(null);

        for(EcgFile file : openedFileList) {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        openedFileList.clear();

        notifyFileListChanged();
    }

    private void notifyFileListChanged() {
        if(listener != null) {
            listener.onFileListChanged(unmodifiedFileList);
        }
    }

    private void notifySelectedFileChanged() {
        if(listener != null) {
            listener.onFileSelected(selectedFile);
        }
    }
}
