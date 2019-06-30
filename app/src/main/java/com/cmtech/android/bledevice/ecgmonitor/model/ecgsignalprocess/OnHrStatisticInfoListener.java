package com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess.ecghrprocess.EcgHrInfoObject;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess
 * ClassName:      OnHrStatisticInfoListener
 * Description:    心率统计信息更新接口
 * Author:         作者名
 * CreateDate:     2018-12-07 07:06
 * UpdateUser:     更新者
 * UpdateDate:     2019-06-15 07:06
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public interface OnHrStatisticInfoListener {
    void onHrStatisticInfoUpdated(EcgHrInfoObject hrInfoObject); // 心率统计信息更新
}
