package com.cmtech.android.bledevice.ecgmonitor.model.ecgrecord;

/**
 * IEcgRecordObserver: 心电记录观察者接口
 * Created by Chenm, 2018-12-27
 */

public interface IEcgRecordObserver {
    void updateRecordSecond(int second); // 更新记录的秒数
}
