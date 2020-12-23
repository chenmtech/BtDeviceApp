package com.cmtech.android.bledeviceapp.util;

import com.vise.log.ViseLog;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_ACCOUNT_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_CHANGE_PASSWORD;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_DATA_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_DELETE_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_DOWNLOAD_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_INVALID_PARA_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_LOGIN_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SIGNUP_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_UPDATE_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_UPLOAD_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;

public class WebFailureHandler {
    public static String toString(int rtnCode) {
        String errString = "";
        switch (rtnCode) {
            case RETURN_CODE_WEB_FAILURE:
                errString = "网络无法连接，请检查您的网络";
                break;

            case RETURN_CODE_INVALID_PARA_ERR:
                errString = "数据传输异常";
                break;

            case RETURN_CODE_SIGNUP_ERR:
                errString = "注册失败，手机号已注册";
                break;

            case RETURN_CODE_LOGIN_ERR:
                errString = "手机号或密码错误，登录失败";
                break;

            case RETURN_CODE_ACCOUNT_ERR:
                errString = "账户信息错误";
                break;

            case RETURN_CODE_UPDATE_ERR:
                errString = "更新失败";
                break;

            case RETURN_CODE_UPLOAD_ERR:
                errString = "上传失败";
                break;

            case RETURN_CODE_DOWNLOAD_ERR:
                errString = "下载失败";
                break;

            case RETURN_CODE_DELETE_ERR:
                errString = "删除失败";
                break;

            case RETURN_CODE_DATA_ERR:
                errString = "数据读取异常";
                break;

            case RETURN_CODE_CHANGE_PASSWORD:
                errString = "修改密码错误";
                break;

            default:
                errString = "未知错误";
                break;
        }
        ViseLog.e("error code: " + rtnCode);
        return errString;
    }
}
