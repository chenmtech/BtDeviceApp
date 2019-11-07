package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.BmeFileUtil;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.sharesdk.framework.PlatformActionListener;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.DIR_WECHAT_DOWNLOAD;
import static com.cmtech.android.bledeviceapp.BleDeviceConstant.DIR_CACHE;

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
    private List<EcgRecord> updatedRecords; // 已更新记录列表
    private volatile EcgRecord selectedRecord; // 被选中的记录
    private final int recordOrder; // 记录排序方式
    private final OnEcgRecordsListener listener; // ECG记录监听器

    public interface OnEcgRecordsListener {
        void onRecordSelected(EcgRecord ecgRecord); // 记录选中
        void onRecordAdded(EcgRecord ecgRecord); // 记录添加
        void onRecordListChanged(List<EcgRecord> recordList); // 记录列表改变
    }

    public EcgRecordExplorer(int recordOrder, OnEcgRecordsListener listener) {
        this.recordOrder = recordOrder;
        this.listener = listener;
        updatedRecords = new ArrayList<>();
        this.recordList = LitePal.findAll(EcgRecord.class, true);
        ViseLog.e(recordList);
        sortRecords(recordOrder);
        List<User> users = LitePal.findAll(User.class);
        ViseLog.e(users);
    }

    // 排序记录
    private void sortRecords(final int recordOrder) {
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
                        time1 = o1.getModifyTime();
                        time2 = o2.getModifyTime();
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
            notifySelectedRecordChanged();
        }
    }

    private void notifySelectedRecordChanged() {
        if(listener != null) {
            listener.onRecordSelected(selectedRecord);
        }
    }

    // 删除选中文件
    public synchronized void deleteSelectRecord() {
        try {
            if(selectedRecord != null) {
                FileUtil.deleteFile(new File(selectedRecord.getSigFileName()));
                selectedRecord.delete();
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
        List<EcgRecord> updatedRecords = importRecords(DIR_WECHAT_DOWNLOAD);
        if(updatedRecords != null && !updatedRecords.isEmpty()) {
            close();
            this.updatedRecords.addAll(updatedRecords);
            sortRecords(recordOrder);
            notifyRecordListChanged();
            return true;
        }
        return false;
    }

    // 从指定文件路径导入Ecg记录
    private List<EcgRecord> importRecords(File dir) {
        List<File> fileList = BmeFileUtil.listDirBmeFiles(dir);
        if(fileList == null || fileList.isEmpty()) return null;
        List<EcgRecord> updatedRecords = new ArrayList<>();
        EcgFile ecgFile = null;
        for(File file : fileList) {
            try {
                ecgFile = EcgFile.open(file.getAbsolutePath());
                if(ecgFile != null) {
                    EcgRecord srcRecord = EcgRecord.create(ecgFile);
                    if (srcRecord != null) {
                        int index = recordList.indexOf(srcRecord);
                        if (index != -1) {
                            EcgRecord destRecord = recordList.get(index);
                            if (updateComments(srcRecord, destRecord)) {
                                destRecord.save();
                                updatedRecords.add(destRecord);
                            }
                        } else {
                            srcRecord.save();
                            recordList.add(srcRecord);
                            updatedRecords.add(srcRecord);
                        }
                    }
                    ecgFile.close();
                }
                FileUtil.deleteFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(ecgFile != null) {
                        ecgFile.close();
                        ecgFile = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return updatedRecords;
    }

    // 通过微信分享选中记录
    public void shareSelectedRecordThroughWechat(PlatformActionListener listener) {
        /*if(selectedRecord == null) return;
        String ecgFileName = selectedRecord.saveAsEcgFile(DIR_CACHE);
        if(ecgFileName == null) return;
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        String fileShortName = selectedRecord.getRecordName();
        sp.setTitle(fileShortName);
        sp.setComment("hi");
        Bitmap bmp = BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.mipmap.ic_kang);
        sp.setImageData(bmp);
        sp.setFilePath(ecgFileName);
        Platform platform = ShareSDK.getPlatform(Wechat.NAME);
        platform.setPlatformActionListener(listener);
        platform.share(sp);*/
        if(selectedRecord == null) return;
        ViseLog.e("hi1");
        String ecgFileName = selectedRecord.saveAsEcgFile(DIR_CACHE);
        if(ecgFileName == null) return;
        ViseLog.e("hi2");
        EcgFile ecgFile = EcgFile.open(ecgFileName);
        ViseLog.e(ecgFile);
        if(ecgFile != null) {
            EcgRecord record = EcgRecord.create(ecgFile);
            ViseLog.e(record);
        }
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
        updatedRecords.clear();
        notifyRecordListChanged();
    }

    // 更新记录留言
    private boolean updateComments(EcgRecord srcRecord, EcgRecord destRecord) {
        List<EcgNormalComment> srcComments = srcRecord.getCommentList();
        List<EcgNormalComment> destComments = destRecord.getCommentList();

        boolean update = false;
        boolean needAdd = true;
        EcgNormalComment tmpComment = null;
        for(EcgNormalComment srcComment : srcComments) {
            for(EcgNormalComment destComment : destComments) {
                if(srcComment.getCreator().equals(destComment.getCreator())) {
                    if(srcComment.getModifyTime() <= destComment.getModifyTime()) {
                        needAdd = false;
                        break;
                    } else {
                        tmpComment = destComment;
                        break;
                    }
                }
            }
            if(needAdd) {
                if(tmpComment != null) {
                    destRecord.deleteComment(tmpComment);
                    tmpComment = null;
                }
                destRecord.addComment(srcComment);
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
}
