package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess
 * ClassName:      OnHrValueUpdateListener
 * Description:    心率值更新接口
 * Author:         作者名
 * CreateDate:     2018-12-23 06:58
 * UpdateUser:     更新者
 * UpdateDate:     2019-06-15 06:58
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public interface OnHrValueUpdateListener {
    void onHrValueUpdated(short hr);
}
