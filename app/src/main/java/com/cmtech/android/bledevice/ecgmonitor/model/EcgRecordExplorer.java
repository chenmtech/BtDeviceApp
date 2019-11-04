package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.BmeFileUtil;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

import static cn.sharesdk.framework.Platform.SHARE_FILE;

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

    private List<EcgRecord> recordList; // 心电记录列表
    private List<EcgRecord> updatedRecords; // 已更新记录
    private volatile EcgRecord selectedRecord; // 被选中的记录
    private final int recordOrder; // 记录排序方式
    private final OnEcgRecordsListener listener; // ECG记录监听器

    public interface OnEcgRecordsListener {
        void onRecordSelected(EcgRecord ecgRecord); // 记录被选中
        void onRecordAdded(EcgRecord ecgRecord); // 添加记录
        void onRecordListChanged(List<EcgRecord> recordList); // 记录列表改变
    }

    public EcgRecordExplorer(int recordOrder, OnEcgRecordsListener listener) {
        this.recordOrder = recordOrder;
        this.listener = listener;
        updatedRecords = new ArrayList<>();
        this.recordList = LitePal.findAll(EcgRecord.class, true);
        ViseLog.e(recordList);
        sortRecordList(recordOrder);
    }

    // 排序记录列表
    private void sortRecordList(final int recordOrder) {
        if(recordList != null && recordList.size() > 1) {
            Collections.sort(recordList, new Comparator<EcgRecord>() {
                @Override
                public int compare(EcgRecord o1, EcgRecord o2) {
                    long time1;
                    long time2;
                    if(recordOrder == ORDER_CREATE_TIME) {
                        time1 = o1.getCreateTime();
                        time2 = o2.getCreateTime();
                    } else {
                        time1 = o1.getLastModifyTime();
                        time2 = o2.getLastModifyTime();
                    }
                    if(time1 == time2) return 0;
                    return (time2 > time1) ? 1 : -1;
                }
            });
        }
    }

    public List<EcgRecord> getRecordList() {
        return recordList;
    }
    public List<EcgRecord> getUpdatedRecords() {
        return updatedRecords;
    }
    public void addUpdatedRecord(EcgRecord record) {
        if(!updatedRecords.contains(record)) {
            updatedRecords.add(record);
        }
    }
    public EcgRecord getSelectedRecord() {
        return selectedRecord;
    }

    // 选中文件
    public void selectRecord(EcgRecord record) {
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
                FileUtil.deleteFile(new File(selectedRecord.getSigFileName()));
                LitePal.delete(EcgRecord.class, selectedRecord.getId());
                if(recordList.remove(selectedRecord)) {
                    notifyRecordListChanged();
                }
                selectRecord(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从微信导入文件
    public boolean importFromWechat() {
        /*List<File> updatedFileList = importUpdatedFiles(DIR_WECHAT_DOWNLOAD, ecgFileDir);
        if(updatedFileList != null && !updatedFileList.isEmpty()) {
            close();
            //updatedRecords.addAll(updatedFileList);
            sortRecordList(recordOrder);
            return true;
        }*/
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
        selectRecord(null);

        for(EcgRecord record : recordList) {
            try {
                record.closeSigFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        recordList.clear();

        notifyRecordListChanged();
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

    private void notifyRecordListChanged() {
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
