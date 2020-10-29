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
    int CMD_REQUEST_REPORT = 0; // request report
    int CMD_DOWNLOAD_REPORT = 1; // get a new report

    int CODE_REPORT_SUCCESS = 0;
    int CODE_REPORT_FAILURE = 1;
    int CODE_REPORT_ADD_NEW = 2;
    int CODE_REPORT_PROCESSING = 3;
    int CODE_REPORT_REQUEST_AGAIN = 4;
    int CODE_REPORT_NO_NEW = 5;

    void retrieveDiagnoseResult(Context context, IWebResponseCallback callback);
    void requestDiagnose();
}
