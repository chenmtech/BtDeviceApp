package com.cmtech.android.bledevice.hrmonitor.model;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      IHRMonitorDeviceListener
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020-02-04 09:35
 * UpdateUser:     更新者
 * UpdateDate:     2020-02-04 09:35
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface IHRMonitorDeviceListener {
    void onHRMeasureUpdated(byte[] hrData);
    void onHRSensLocUpdated(int loc);
    void onHRCtrlPtUpdated(int ctrl);
}
