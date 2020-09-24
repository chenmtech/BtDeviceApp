package com.cmtech.android.bledevice.report;

import android.content.Context;

import com.cmtech.android.bledeviceapp.interfac.IWebOperationCallback;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.report
 * ClassName:      IReportOperatable
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/9/25 上午5:55
 * UpdateUser:     更新者
 * UpdateDate:     2020/9/25 上午5:55
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface IReportOperation {
    void refreshReport(Context context, IWebOperationCallback callback);
    void requestReport(Context context, IWebOperationCallback callback);
}
