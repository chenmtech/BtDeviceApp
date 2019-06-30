package com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess
 * ClassName:      OnSignalValueUpdateListener
 * Description:    信号值更新接口
 * Author:         作者名
 * CreateDate:     2018-12-23 06:55
 * UpdateUser:     更新者
 * UpdateDate:     2019-06-15 06:55
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public interface OnSignalValueUpdateListener {
    void onSignalValueUpdated(int ecgSignal);
}
