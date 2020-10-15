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
    int RETURN_CODE_OTHER_ERR = 10;
    void upload(Context context, IWebCallback callback); // upload a new record or update the record info
    void download(Context context, IWebCallback callback); // download the info
    void delete(Context context, IWebCallback callback); // delete the record
    void retrieveList(Context context, int num, String queryStr, long fromTime, IWebCallback callback); // retrieve record list according to the conditions
}
