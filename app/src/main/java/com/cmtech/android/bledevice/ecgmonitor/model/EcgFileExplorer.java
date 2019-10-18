package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStatisticsInfo;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.util.BmeFileUtil;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    private final OpenedEcgFilesManager filesManager; // 文件列表管理器
    private final File ecgFileDir; // Ecg文件路径
    private Iterator<File> fileIterator; // 文件迭代器
    private List<File> updatedFiles;
    private final ExecutorService openFileService = Executors.newSingleThreadExecutor(); // 打开文件服务

    public EcgFileExplorer(File ecgFileDir, OpenedEcgFilesManager.OnOpenedEcgFilesListener listener) throws IOException{
        if(ecgFileDir == null) {
            throw new IOException("The ecg file dir is null");
        }
        if(!ecgFileDir.exists() && !ecgFileDir.mkdir()) {
            throw new IOException("The ecg file dir doesn't exist.");
        }
        if(ecgFileDir.exists() && !ecgFileDir.isDirectory()) {
            throw new IOException("The ecg file dir is invalid.");
        }

        filesManager = new OpenedEcgFilesManager(listener);
        this.ecgFileDir = ecgFileDir;
        initFileIterator();
        updatedFiles = new ArrayList<>();
    }

    // 初始化文件迭代器，文件按照创建时间排序
    private void initFileIterator() {
        List<File> fileList = BmeFileUtil.listDirBmeFiles(ecgFileDir);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String f1 = o1.getName();
                String f2 = o2.getName();
                long createdTime1 = Long.parseLong(f1.substring(MACADDRESS_CHAR_NUM, f1.length()-4));
                long createdTime2 = Long.parseLong(f2.substring(MACADDRESS_CHAR_NUM, f2.length()-4));
                if(createdTime1 == createdTime2) return 0;
                return (createdTime2 > createdTime1) ? 1 : -1;
            }
        });
        fileIterator = fileList.iterator();
    }

    public int loadNextFiles(int num) {
        int i = 0;
        while(i < num && fileIterator.hasNext()) {
            File file = fileIterator.next();
            openFile(file);
            i++;
        }
        return i;
    }

    private void openFile(File file) {
        openFileService.execute(new OpenFileRunnable(file));
    }

    // 选中文件
    public void selectFile(EcgFile ecgFile) {
        if(ecgFile != null)
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
        List<File> changed = importFiles(weChatDir, ecgFileDir);
        if(changed != null && !changed.isEmpty()) {
            updatedFiles.addAll(changed);
        }
        initFileIterator();
    }

    // 导入新文件或者修改发生变化的文件
    private List<File> importFiles(File srcDir, File destDir) {
        List<File> fileList = BmeFileUtil.listDirBmeFiles(srcDir);
        if(fileList == null || fileList.isEmpty()) return null;

        List<File> changedFiles = new ArrayList<>();
        EcgFile srcEcgFile = null;
        EcgFile destEcgFile = null;
        for(File srcFile : fileList) {
            try {
                srcEcgFile = EcgFile.open(srcFile.getCanonicalPath());
                String fileName = EcgMonitorUtil.makeFileName(srcEcgFile.getMacAddress(), srcEcgFile.getCreatedTime());
                File destFile = FileUtil.getFile(destDir, fileName);
                if(destFile.exists()) {
                    destEcgFile = EcgFile.open(destFile.getCanonicalPath());
                    if(copyComments(srcEcgFile, destEcgFile)) {
                        destEcgFile.saveFileTail();
                        changedFiles.add(destFile);
                    }
                } else {
                    FileUtil.copyFile(srcFile, destFile);
                    changedFiles.add(destFile);
                }
                srcEcgFile.close();
                srcEcgFile = null;
                FileUtil.deleteFile(srcFile);
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
        return changedFiles;
    }

    // 用微信分享一个文件
    public void shareSelectedFileThroughWechat(final Context context) {
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

    public EcgHrStatisticsInfo getSelectedFileHrStatisticsInfo() {
        return filesManager.getSelectedFileHrStatisticsInfo();
    }

    public List<File> getUpdatedFiles() {
        return updatedFiles;
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
        EcgNormalComment removeComment = null;
        for(EcgNormalComment srcComment : srcComments) {
            for(EcgNormalComment destComment : destComments) {
                if(srcComment.getCreator().equals(destComment.getCreator())) {
                    if(srcComment.getModifyTime() <= destComment.getModifyTime()) {
                        needAdd = false;
                        break;
                    } else {
                        removeComment = destComment;
                        break;
                    }
                }
            }
            if(needAdd) {
                if(removeComment != null) {
                    destFile.deleteComment(removeComment);
                    removeComment = null;
                }
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
