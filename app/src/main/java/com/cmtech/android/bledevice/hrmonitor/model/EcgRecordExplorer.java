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

public class EcgRecordExplorer {
    private List<BleEcgRecord10> allRecords; // 所有心电记录列表
    private volatile BleEcgRecord10 selRecord; // 被选中的记录
    private final OnEcgRecordsListener listener; // ECG记录监听器

    public interface OnEcgRecordsListener {
        void onRecordSelected(BleEcgRecord10 ecgRecord); // 记录选中
        void onRecordListChanged(); // 记录列表改变
    }

    public EcgRecordExplorer(OnEcgRecordsListener listener) {
        this.listener = listener;
        this.allRecords = LitePal.findAll(BleEcgRecord10.class, true);
        if(allRecords != null && allRecords.size() > 1) {
            Collections.sort(allRecords, new Comparator<BleEcgRecord10>() {
                @Override
                public int compare(BleEcgRecord10 o1, BleEcgRecord10 o2) {
                    long time1 = o1.getCreateTime();
                    long time2 = o2.getCreateTime();
                    if(time1 == time2) return 0;
                    return (time2 > time1) ? 1 : -1;
                }
            });
        }
    }

    public List<BleEcgRecord10> getAllRecords() {
        return allRecords;
    }
    public BleEcgRecord10 getSelRecord() {
        return selRecord;
    }

    // 选中文件
    public void selectRecord(BleEcgRecord10 record) {
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
