package com.cmtech.android.bledeviceapp.data.record;

import android.content.Context;

import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.report
 * ClassName:      IDiagnosable
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/9/25 上午5:55
 * UpdateUser:     更新者
 * UpdateDate:     2020/9/25 上午5:55
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface IDiagnosable {
    // 请求远端诊断
    void remoteDiagnose(Context context, IWebResponseCallback callback);

    // 本地端诊断
    void localDiagnose();
}
