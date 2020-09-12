package com.cmtech.android.bledevice.record;

import android.content.Context;

import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.interfac.IWebOperationCallback;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.record
 * ClassName:      IRecordWebOperation
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/9/12 上午7:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/9/12 上午7:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
interface IRecordWebOperation extends IWebOperation {
    void delete(Context context, IWebOperationCallback callback);
    void query(Context context, long fromTime, String queryStr, int num, IWebOperationCallback callback);
}
