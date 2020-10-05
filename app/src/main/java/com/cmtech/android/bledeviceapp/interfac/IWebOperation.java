package com.cmtech.android.bledeviceapp.interfac;

import android.content.Context;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.interfac
 * ClassName:      IWebOperatable
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/9/12 上午5:46
 * UpdateUser:     更新者
 * UpdateDate:     2020/9/12 上午5:46
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface IWebOperation {
    int SUCCESS = 0;
    int FAILURE = 1;
    void upload(Context context, IWebCallback callback);
    void download(Context context, IWebCallback callback);
    void delete(Context context, IWebCallback callback);
    void query(Context context, long fromTime, String queryStr, int num, IWebCallback callback);
}
