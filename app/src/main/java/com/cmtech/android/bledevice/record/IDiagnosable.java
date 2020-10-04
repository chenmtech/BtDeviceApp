package com.cmtech.android.bledevice.record;

import android.content.Context;

import com.cmtech.android.bledeviceapp.interfac.IWebOperationCallback;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.report
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
    int REPORT_CMD_REQUEST = 0; // request report
    int REPORT_CMD_DOWNLOAD = 1; // get a new report

    int CODE_REPORT_SUCCESS = 0;
    int CODE_REPORT_FAILURE = 1;
    int CODE_REPORT_ADD_NEW = 2;
    int CODE_REPORT_PROCESSING = 3;
    int CODE_REPORT_REQUEST_AGAIN = 4;
    int CODE_REPORT_NO_NEW = 5;

    void retrieveDiagnoseResult(Context context, IWebOperationCallback callback);
    void requestDiagnose(Context context, IWebOperationCallback callback);
}
