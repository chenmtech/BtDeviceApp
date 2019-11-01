package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.BmeFileUtil;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

import static cn.sharesdk.framework.Platform.SHARE_FILE;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.DIR_WECHAT_DOWNLOAD;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead.MACADDRESS_CHAR_NUM;

/**
  *
  * ClassName:      EcgRecordExplorer
  * Description:    Ecg记录浏览器类
  * Author:         chenm
  * CreateDate:     2018/11/10 下午4:09
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午4:09
  * UpdateRemark:   用类图优化代码
  * Version:        1.0
 */

public class EcgRecordExplorer {
    public static final int FILE_ORDER_CREATED_TIME = 0; // 文件按创建时间排序
    public static final int FILE_ORDER_MODIFIED_TIME = 1; // 文件按修改时间排序

    private final File ecgFileDir; // Ecg文件路径
    private final List<EcgFile> fileList = new ArrayList<>(); // 心电文件列表
    private final List<EcgFile> unmodifiedFileList = Collections.unmodifiableList(fileList);
    private Iterator<File> fileIterator; // 文件迭代器
    private List<File> updatedFiles; // 已更新文件
    private volatile EcgFile selectedFile; // 被选中的EcgFile
    private final int fileOrder;
    private final ExecutorService openFileService = Executors.newSingleThreadExecutor(); // 打开文件服务
    private final OnEcgFilesListener listener; // ECG文件监听器

    public interface OnEcgFilesListener {
        void onFileSelected(EcgFile ecgFile); // 文件被选中
        void onNewFileAdded(EcgFile ecgFile); // 添加新文件
        void onFileListChanged(List<EcgFile> fileList); // 文件列表改变
    }

    public EcgRecordExplorer(File ecgFileDir, int fileOrder, OnEcgFilesListener listener) throws IOException{
        if(ecgFileDir == null) {
            throw new IOException("The ecg file dir is null");
        }
        if(!ecgFileDir.exists() && !ecgFileDir.mkdir()) {
            throw new IOException("The ecg file dir doesn't exist.");
        }
        if(ecgFileDir.exists() && !ecgFileDir.isDirectory()) {
            throw new IOException("The ecg file dir is invalid.");
        }

        this.ecgFileDir = ecgFileDir;
        this.fileOrder = fileOrder;
        this.listener = listener;
        updatedFiles = new ArrayList<>();
        updateFileIterator(fileOrder);
    }

    // 获取文件列表，并排序
    private void updateFileIterator(final int fileOrder) {
        List<File> fileList = BmeFileUtil.listDirBmeFiles(ecgFileDir);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                long time1;
                long time2;
                if(fileOrder == FILE_ORDER_CREATED_TIME) {
                    String f1 = o1.getName();
                    String f2 = o2.getName();
                    time1 = Long.parseLong(f1.substring(MACADDRESS_CHAR_NUM, f1.length()-4));
                    time2 = Long.parseLong(f2.substring(MACADDRESS_CHAR_NUM, f2.length()-4));
                } else {
                    time1 = o1.lastModified();
                    time2 = o2.lastModified();
                }
                if(time1 == time2) return 0;
                return (time2 > time1) ? 1 : -1;
            }
        });
        if(fileList != null)
            fileIterator = fileList.iterator();
        else
            fileIterator = null;
    }

    public List<EcgFile> getFileList() {
        return unmodifiedFileList;
    }
    public List<File> getUpdatedFiles() {
        return updatedFiles;
    }
    public void addUpdatedFile(File file) {
        if(!updatedFiles.contains(file)) {
            updatedFiles.add(file);
        }
    }
    public EcgFile getSelectedFile() {
        return selectedFile;
    }

    public int loadNextFiles(int num) {
        if(fileIterator == null) return 0;

        int i = 0;
        while(i < num && fileIterator.hasNext()) {
            File file = fileIterator.next();
            openFileService.execute(new LoadFileRunnable(file));
            i++;
        }
        return i;
    }

    // 选中文件
    public void selectFile(EcgFile file) {
        if(selectedFile != file) {
            selectedFile = file;
            notifySelectedFileChanged();
        }
    }

    // 删除选中文件
    public void deleteSelectFile(Context context) {
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
                if(fileList.remove(selectedFile)) {
                    notifyFileListChanged();
                }
                selectFile(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从微信导入文件
    public boolean importFromWechat() {
        List<File> updatedFileList = importUpdatedFiles(DIR_WECHAT_DOWNLOAD, ecgFileDir);
        if(updatedFileList != null && !updatedFileList.isEmpty()) {
            close();
            updatedFiles.addAll(updatedFileList);
            updateFileIterator(fileOrder);
            return true;
        }
        return false;
    }

    // 导入新文件或者发生变化的文件
    private List<File> importUpdatedFiles(File srcDir, File destDir) {
        List<File> fileList = BmeFileUtil.listDirBmeFiles(srcDir);
        if(fileList == null || fileList.isEmpty()) return null;

        List<File> changedFiles = new ArrayList<>();
        EcgFile srcEcgFile = null;
        EcgFile destEcgFile = null;
        for(File srcFile : fileList) {
            try {
                srcEcgFile = EcgFile.open(srcFile.getCanonicalPath());
                String fileName = EcgMonitorUtil.makeFileName(srcEcgFile.getMacAddress(), srcEcgFile.getCreateTime());
                File destFile = FileUtil.getFile(destDir, fileName);
                ViseLog.e("srcfile = " + srcFile);
                ViseLog.e("destFile = " + destFile);
                if(destFile.exists()) {
                    destEcgFile = EcgFile.open(destFile.getCanonicalPath());
                    if(copyComments(srcEcgFile, destEcgFile)) {
                        destEcgFile.saveFileTail();
                        changedFiles.add(destFile);
                    }
                } else {
                    srcEcgFile.close();
                    FileUtil.moveFile(srcFile, destFile);
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

    // 通过微信分享选中文件
    public void shareSelectedFileThroughWechat(final Context context) {
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
    public synchronized void close() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(openFileService);

        selectFile(null);

        for(EcgFile file : fileList) {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileList.clear();

        notifyFileListChanged();
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

    private class LoadFileRunnable implements Runnable {
        private final File file;

        LoadFileRunnable(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            try {
                loadFileInfo(file);
            } catch (IOException e) {
                ViseLog.e("The file is wrong: " + file);
            }
        }
    }


    // 打开文件
    private synchronized void loadFileInfo(File file) throws IOException{
        boolean contain = false;
        for(EcgFile ecgFile : fileList) {
            if(ecgFile.getFile().getName().equalsIgnoreCase(file.getName())) {
                contain = true;
                break;
            }
        }

        if(!contain) {
            EcgFile ecgFile = EcgFile.open(file.getCanonicalPath());
            ecgFile.close();
            fileList.add(ecgFile);
            if(listener != null)
                listener.onNewFileAdded(ecgFile);
        }
    }

    private void notifyFileListChanged() {
        if(listener != null) {
            listener.onFileListChanged(fileList);
        }
    }

    private void notifySelectedFileChanged() {
        if(listener != null) {
            listener.onFileSelected(selectedFile);
        }
    }
}
