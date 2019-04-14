package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.ecgmonitor.activity.EcgFileExplorerActivity;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.vise.log.ViseLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

import static cn.sharesdk.framework.Platform.SHARE_FILE;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead.MACADDRESS_CHAR_NUM;

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
        private File file;

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

    private final File ecgFileDir; // Ecg文件目录

    private final File[] allEcgFiles;

    private EcgFile selectFile; // 选中的EcgFile

    private EcgFilesManager filesManager; // 文件列表管理器

    private EcgHrRecorder hrRecorder;

    private OnEcgFileExploreListener listener; // 文件浏览监听器

    private final ExecutorService openFileService = Executors.newSingleThreadExecutor();

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

        /*Arrays.sort(allEcgFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String f1 = o1.getName();
                String f2 = o2.getName();
                long createTime1 = Long.parseLong(f1.substring(MACADDRESS_CHAR_NUM, f1.length()-4));
                long createTime2 = Long.parseLong(f2.substring(MACADDRESS_CHAR_NUM, f2.length()-4));
                if(createTime1 == createTime2) return 0;
                return (createTime2 > createTime1) ? 1 : -1;
            }
        });*/

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
    public void changeSelectFile(EcgFile ecgFile) {
        if(ecgFile == null) return;

        filesManager.select(ecgFile);
    }

    // 删除选中文件
    public void deleteSelectFile(Context context) {
        if(selectFile != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

    // 用微信分享一个文件
    public void shareSelectFileThroughWechat(final Context context) {
        if(selectFile == null) return;

        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        String fileShortName = selectFile.getFile().getName();
        sp.setTitle(fileShortName);
        sp.setText(fileShortName);
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_kang);
        sp.setImageData(bmp);
        sp.setFilePath(selectFile.getFileName());

        Platform wxPlatform = ShareSDK.getPlatform(Wechat.NAME);
        wxPlatform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Toast.makeText(context, "分享成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                //Toast.makeText(EcgFileExplorerActivity.this, "分享错误", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                //Toast.makeText(EcgFileExplorerActivity.this, "分享取消", Toast.LENGTH_SHORT).show();
            }
        });
        // 执行分享
        wxPlatform.share(sp);
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

    public void getHrInfo() {
        if(selectFile != null) {
            hrRecorder.setHrList(selectFile.getHrList());
            hrRecorder.updateHrInfo(10, 5);
        }
    }

    public void close() {
        filesManager.close();

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
