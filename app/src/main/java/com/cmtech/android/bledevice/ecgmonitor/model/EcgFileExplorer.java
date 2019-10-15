package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.HrStatisticProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.util.BmeFileUtil;
import com.vise.log.ViseLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead.MACADDRESS_CHAR_NUM;

/**
  *
  * ClassName:      EcgFileExplorer
  * Description:    Ecg文件浏览器类
  * Author:         chenm
  * CreateDate:     2018/11/10 下午4:09
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午4:09
  * UpdateRemark:   用类图优化代码
  * Version:        1.0
 */

public class EcgFileExplorer {
    private final static int FILENUM_LOADED_EACH_TIMES = 5;

    public interface OnEcgFileExplorerListener extends EcgFilesManager.OnEcgFilesChangedListener, HrStatisticProcessor.OnHrStatisticInfoUpdatedListener {
    }

    private final File ecgFileDir; // Ecg文件路径
    private final List<File> fileList;
    private Iterator<File> fileIterator;
    private final EcgFilesManager filesManager; // 文件列表管理器
    private final OnEcgFileExplorerListener listener; // 文件浏览监听器
    private final ExecutorService openFileService = Executors.newSingleThreadExecutor();

    public EcgFileExplorer(File ecgFileDir, OnEcgFileExplorerListener listener) throws IOException{
        if(ecgFileDir == null) {
            throw new IOException("The ecg file dir is null");
        }
        if(!ecgFileDir.exists() && !ecgFileDir.mkdir()) {
            throw new IOException("The ecg file dir doesn't exist.");
        }
        if(ecgFileDir.exists() && !ecgFileDir.isDirectory()) {
            throw new IOException("The ecg file dir is invalid.");
        }

        filesManager = new EcgFilesManager(listener);
        this.ecgFileDir = ecgFileDir;
        File[] files = BmeFileUtil.listDirBmeFiles(ecgFileDir); // 列出所有bme文件
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String f1 = o1.getName();
                String f2 = o2.getName();
                long createTime1 = Long.parseLong(f1.substring(MACADDRESS_CHAR_NUM, f1.length()-4));
                long createTime2 = Long.parseLong(f2.substring(MACADDRESS_CHAR_NUM, f2.length()-4));
                if(createTime1 == createTime2) return 0;
                return (createTime2 > createTime1) ? 1 : -1;
            }
        });
        fileList = new ArrayList<>(Arrays.asList(files));
        fileIterator = fileList.iterator();
        this.listener = listener;
    }

    public void loadNextFiles() {
        loadNextFiles(FILENUM_LOADED_EACH_TIMES);
    }

    private void loadNextFiles(int num) {
        int i = 0;
        while(i < num && fileIterator.hasNext()) {
            File file = fileIterator.next();
            openFile(file);
            i++;
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
        filesManager.importToFromWechat(ecgFileDir);
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
            listener.onHrStatisticInfoUpdated(filesManager.getSelectFileHrInfo());

    }

    public void close() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(openFileService);
        filesManager.close();
    }

    private class OpenFileRunnable implements Runnable {
        private final File file;

        OpenFileRunnable(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            try {
                filesManager.openFile(file);
            } catch (IOException e) {
                ViseLog.e("To open ecg file is wrong." + file);
            }
        }
    }
}
