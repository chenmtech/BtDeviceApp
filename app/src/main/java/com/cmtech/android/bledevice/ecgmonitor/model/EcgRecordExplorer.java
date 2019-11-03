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
    public static final int ORDER_CREATE_TIME = 0; // 按创建时间排序
    public static final int ORDER_MODIFY_TIME = 1; // 按修改时间排序

    private final File ecgFileDir; // Ecg文件路径
    private final List<EcgRecord> recordList = new ArrayList<>(); // 心电记录列表
    private final List<EcgRecord> unmodifiedRecordList = Collections.unmodifiableList(recordList);
    private Iterator<File> fileIterator; // 文件迭代器
    private List<EcgRecord> updatedRecords; // 已更新文件
    private volatile EcgRecord selectedRecord; // 被选中的记录
    private final int fileOrder;
    private final ExecutorService openFileService = Executors.newSingleThreadExecutor(); // 打开文件服务
    private final OnEcgRecordsListener listener; // ECG文件监听器

    public interface OnEcgRecordsListener {
        void onRecordSelected(EcgRecord ecgRecord); // 文件被选中
        void onNewRecordAdded(EcgRecord ecgRecord); // 添加新文件
        void onRecordListChanged(List<EcgRecord> recordList); // 文件列表改变
    }

    public EcgRecordExplorer(File ecgFileDir, int fileOrder, OnEcgRecordsListener listener) throws IOException{
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
        updatedRecords = new ArrayList<>();
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
                if(fileOrder == ORDER_CREATE_TIME) {
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

    public List<EcgRecord> getRecordList() {
        return unmodifiedRecordList;
    }
    public List<EcgRecord> getUpdatedRecords() {
        return updatedRecords;
    }
    public void addUpdatedFile(EcgRecord record) {
        if(!updatedRecords.contains(record)) {
            updatedRecords.add(record);
        }
    }
    public EcgRecord getSelectedRecord() {
        return selectedRecord;
    }

    public int loadNextRecords(int num) {
        if(fileIterator == null) return 0;

        int i = 0;
        while(i < num && fileIterator.hasNext()) {
            File file = fileIterator.next();
            //openFileService.execute(new LoadFileRunnable(file));
            i++;
        }
        return i;
    }

    // 选中文件
    public void selectFile(EcgRecord record) {
        if(selectedRecord != record) {
            selectedRecord = record;
            notifySelectedFileChanged();
        }
    }

    // 删除选中记录
    public void deleteSelectRecord(Context context) {
        if(selectedRecord != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("删除心电记录").setMessage("确定删除该心电记录吗？");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doDeleteSelectRecord();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }
    }

    // 删除文件
    private synchronized void doDeleteSelectRecord() {
        try {
            if(selectedRecord != null) {
                FileUtil.deleteFile(selectedRecord.getFile());
                if(recordList.remove(selectedRecord)) {
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
            //updatedRecords.addAll(updatedFileList);
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
        if(selectedRecord == null) return;
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        String fileShortName = selectedRecord.getRecordName();
        sp.setTitle(fileShortName);
        sp.setComment("hi");
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_kang);
        sp.setImageData(bmp);
        //sp.setFilePath(selectedRecord.getFileName());
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

        for(EcgRecord record : recordList) {
            try {
                record.closeSigFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        recordList.clear();

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
        private final EcgRecord record;

        LoadFileRunnable(EcgRecord record) {
            this.record = record;
        }

        @Override
        public void run() {
            try {
                loadRecord(record);
            } catch (IOException e) {
                ViseLog.e("The record is wrong: " + record);
            }
        }
    }


    // 加载记录
    private synchronized void loadRecord(EcgRecord record) throws IOException{
        boolean contain = false;
        for(EcgRecord ele : recordList) {
            if(ele.equals(record)) {
                contain = true;
                break;
            }
        }

        if(!contain) {
            recordList.add(record);
            if(listener != null)
                listener.onNewRecordAdded(record);
        }
    }

    private void notifyFileListChanged() {
        if(listener != null) {
            listener.onRecordListChanged(recordList);
        }
    }

    private void notifySelectedFileChanged() {
        if(listener != null) {
            listener.onRecordSelected(selectedRecord);
        }
    }
}
