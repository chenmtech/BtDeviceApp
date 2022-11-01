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
    int RETURN_CODE_SUCCESS = 0; // web code success
    int RETURN_CODE_WEB_FAILURE = 1; // web code failure
    int RETURN_CODE_INVALID_PARA_ERR = 2;
    int RETURN_CODE_SIGNUP_ERR = 3;
    int RETURN_CODE_LOGIN_ERR = 4;
    int RETURN_CODE_ACCOUNT_ERR = 5;
    int RETURN_CODE_UPDATE_ERR = 6;
    int RETURN_CODE_UPLOAD_ERR = 7;
    int RETURN_CODE_DOWNLOAD_ERR = 8;
    int RETURN_CODE_DELETE_ERR = 9;
    int RETURN_CODE_DATA_ERR = 10;
    int RETURN_CODE_CHANGE_PASSWORD = 11;

    // 上传或者更新
    void upload(Context context, ICodeCallback callback);

    // 下载
    void download(Context context, String showStr, ICodeCallback callback);

    // 删除
    void delete(Context context, ICodeCallback callback);
}
