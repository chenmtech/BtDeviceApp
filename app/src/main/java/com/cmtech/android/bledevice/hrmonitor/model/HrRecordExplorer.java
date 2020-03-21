package com.cmtech.android.bledevice.hrmonitor.model;

import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
  *
  * ClassName:      HrRecordExplorer
  * Description:    Ecg记录浏览器类
  * Author:         chenm
  * CreateDate:     2018/11/10 下午4:09
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午4:09
  * UpdateRemark:   用类图优化代码
  * Version:        1.0
 */

public class HrRecordExplorer {
    private List<BleHrRecord10> allRecords; // 所有心电记录列表
    private volatile BleHrRecord10 selRecord; // 被选中的记录
    private final OnHrRecordsListener listener; // ECG记录监听器

    public interface OnHrRecordsListener {
        void onRecordSelected(BleHrRecord10 ecgRecord); // 记录选中
        void onRecordListChanged(); // 记录列表改变
    }

    public HrRecordExplorer(OnHrRecordsListener listener) {
        this.listener = listener;
        this.allRecords = LitePal.findAll(BleHrRecord10.class, true);
        ViseLog.e(allRecords);
        if(allRecords != null && allRecords.size() > 1) {
            Collections.sort(allRecords, new Comparator<BleHrRecord10>() {
                @Override
                public int compare(BleHrRecord10 o1, BleHrRecord10 o2) {
                    long time1 = o1.getCreateTime();
                    long time2 = o2.getCreateTime();
                    if(time1 == time2) return 0;
                    return (time2 > time1) ? 1 : -1;
                }
            });
        }
    }

    public List<BleHrRecord10> getAllRecords() {
        return allRecords;
    }
    public BleHrRecord10 getSelRecord() {
        return selRecord;
    }

    // 选中文件
    public void selectRecord(BleHrRecord10 record) {
        if(selRecord != record) {
            selRecord = record;
        }
        if(listener != null) {
            listener.onRecordSelected(selRecord);
        }
    }

    // 删除选中文件
    public synchronized void deleteSelRecord() {
        if(selRecord != null) {
            selRecord.delete();
            if(allRecords.remove(selRecord)) {
                notifyRecordListChanged();
            }
            selectRecord(null);
        }
    }

    // 关闭管理器
    public synchronized void close() {
        selectRecord(null);
        allRecords.clear();
        notifyRecordListChanged();
    }

    private void notifyRecordListChanged() {
        if(listener != null) {
            listener.onRecordListChanged();
        }
    }
}
