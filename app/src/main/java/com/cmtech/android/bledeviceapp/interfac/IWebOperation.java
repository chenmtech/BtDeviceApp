package com.cmtech.android.bledeviceapp.interfac;

import android.content.Context;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.interfac
 * ClassName:      IWebOperatable
 * Description:    网络操作接口
 * Author:         chenm
 * CreateDate:     2020/9/12 上午5:46
 * UpdateUser:     chenm
 * UpdateDate:     2020/9/12 上午5:46
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface IWebOperation {
    int RCODE_SUCCESS = 0; // web code success
    int RCODE_WEB_FAILURE = 1; // web code failure
    int RCODE_INVALID_PARA_ERR = 2;
    int RCODE_DATA_ERR = 3;

    // 上传或者更新
    void upload(Context context, ICodeCallback callback);

    // 下载
    void download(Context context, String showStr, ICodeCallback callback);

    // 删除
    void delete(Context context, ICodeCallback callback);
}
