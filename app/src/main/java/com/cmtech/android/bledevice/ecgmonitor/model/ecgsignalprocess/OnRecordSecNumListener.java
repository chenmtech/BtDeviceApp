package com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess
 * ClassName:      OnRecordSecNumListener
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2019-06-15 08:18
 * UpdateUser:     更新者
 * UpdateDate:     2019-06-15 08:18
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public interface OnRecordSecNumListener {
    void onRecordSecNumUpdated(int second); // 更新心电信号记录的秒数
}
