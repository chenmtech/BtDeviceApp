package com.cmtech.android.bledevice.ecgmonitor.model.ecg1mvcalivaluecalculate;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess
 * ClassName:      On1mVCaliValueListener
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2019-06-15 08:15
 * UpdateUser:     更新者
 * UpdateDate:     2019-06-15 08:15
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface On1mVCaliValueListener {
    void on1mVCaliValueUpdated(int caliValue1mV); // 1mV标定值更新
}
