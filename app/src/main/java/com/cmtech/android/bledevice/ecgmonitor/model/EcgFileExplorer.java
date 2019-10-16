package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.HrStatisticProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.util.BmeFileUtil;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.WECHAT_DOWNLOAD_DIR;
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
    private final File ecgFileDir; // Ecg文件路径
    private final EcgFilesManager filesManager; // 文件列表管理器
    private Iterator<File> fileIterator;
    private final ExecutorService openFileService = Executors.newSingleThreadExecutor();
    private final HrStatisticProcessor.OnHrStatisticInfoUpdatedListener listener; // 文件浏览监听器
    public interface OnEcgFileExplorerListener extends EcgFilesManager.OnEcgFilesChangedListener, HrStatisticProcessor.OnHrStatisticInfoUpdatedListener {
    }

    public EcgFileExplorer(File ecgFileDir, OnEcgFileExplorerListener listener) {
        if(ecgFileDir == null) {
            throw new IllegalArgumentException("The ecg file dir is null");
        }
        if(!ecgFileDir.exists() && !ecgFileDir.mkdir()) {
            throw new IllegalStateException("The ecg file dir doesn't exist.");
        }
        if(ecgFileDir.exists() && !ecgFileDir.isDirectory()) {
            throw new IllegalStateException("The ecg file dir is invalid.");
        }

        filesManager = new EcgFilesManager(listener);
        this.ecgFileDir = ecgFileDir;
        initFileIterator();

        this.listener = listener;
    }

    private void initFileIterator() {
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
        List<File> fileList = new ArrayList<>(Arrays.asList(files));
        fileIterator = fileList.iterator();
    }

    public void loadNextFiles(int num) {
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
        filesManager.deleteSelectedFile(context);
    }

    // 从微信导入文件
    public void importFromWechat() {
        filesManager.close();
        File weChatDir = new File(WECHAT_DOWNLOAD_DIR);
        importFiles(weChatDir, ecgFileDir);
        initFileIterator();
    }

    private void importFiles(File srcDir, File destDir) {
        File[] fileList = BmeFileUtil.listDirBmeFiles(srcDir);
        boolean changed = false;
        EcgFile srcEcgFile = null;
        EcgFile destEcgFile = null;
        for(File srcFile : fileList) {
            try {
                File destFile = FileUtil.getFile(destDir, srcFile.getName());
                if(destFile.exists()) {
                    srcEcgFile = EcgFile.open(srcFile.getCanonicalPath());
                    destEcgFile = EcgFile.open(destFile.getCanonicalPath());
                    if(copyComments(srcEcgFile, destEcgFile)) {
                        changed = true;
                        destEcgFile.saveFileTail();
                    }
                } else {
                    FileUtil.copyFile(srcFile, destFile);
                    changed = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(srcEcgFile != null) {
                        srcEcgFile.close();
                        srcEcgFile = null;
                    }

                    if(destEcgFile != null) {
                        destEcgFile.close();
                        destEcgFile = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 用微信分享一个文件
    public void shareSelectFileThroughWechat(final Context context) {
        filesManager.shareSelectedFileThroughWechat(context);
    }

    // 保存留言信息
    public void saveSelectedFileComment() {
        try {
            filesManager.saveSelectedFileComment();
        } catch (IOException e) {
            ViseLog.e("保存留言错误。");
        }
    }

    public void getSelectedFileHrStatisticsInfo() {
        if(listener != null)
            listener.onHrStatisticInfoUpdated(filesManager.getSelectedFileHrStatisticsInfo());
    }

    public void close() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(openFileService);
        filesManager.close();
    }

    // 拷贝文件留言
    private boolean copyComments(EcgFile srcFile, EcgFile destFile) {
        List<EcgNormalComment> srcComments = srcFile.getCommentList();
        List<EcgNormalComment> destComments = destFile.getCommentList();

        boolean update = false;
        boolean needAdd = true;
        for(EcgNormalComment srcComment : srcComments) {
            for(EcgNormalComment destComment : destComments) {
                if(srcComment.getCreator().equals(destComment.getCreator()) && srcComment.getModifyTime() <= destComment.getModifyTime()) {
                    needAdd = false;
                    break;
                }
            }
            if(needAdd) {
                destFile.addComment(srcComment);
                update = true;
            }
            needAdd = true;
        }
        return update;
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
                ViseLog.e("The file is wrong: " + file);
            }
        }
    }
}
