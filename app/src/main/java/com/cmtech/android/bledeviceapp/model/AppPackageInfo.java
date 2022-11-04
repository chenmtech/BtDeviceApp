package com.cmtech.android.bledeviceapp.model;

import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_CACHE;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_DATA_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_DOWNLOAD_APP_INFO;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.util.UploadDownloadFileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      AppPackageInfo
 * Description:    应用程序安装包信息
 * Author:         chenm
 * CreateDate:     2020/10/28 上午6:28
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/28 上午6:28
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class AppPackageInfo implements Serializable, IJsonable {
    private int verCode;        // 版本号
    private String verName;     // 版本名
    private String note;        // 备注
    private String url;         // 安装包下载URL
    private double size;        // 安装包大小，单位: MB

    public AppPackageInfo() {

    }

    public int getVerCode() {
        return verCode;
    }

    public String getVerName() {
        return verName;
    }

    public String getNote() {
        return note;
    }

    public double getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException {
        verCode = json.getInt("verCode");
        verName = json.getString("verName");
        note = json.getString("note");
        url = json.getString("url");
        size = json.getDouble("size");
    }

    @Override
    public JSONObject toJson() throws JSONException {
        throw new JSONException("Cannot use toJson of AppPackageInfo");
    }

    // 下载最新的app版本信息
    public void download(Context context, String showStr, ICodeCallback callback) {
        new WebAsyncTask(context, showStr, CMD_DOWNLOAD_APP_INFO, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                String msg = response.getMsg();
                if (code == RCODE_SUCCESS) {
                    JSONObject json = (JSONObject) response.getContent();
                    if (json != null) {
                        try {
                            fromJson(json);
                        } catch (JSONException e) {
                            code = RCODE_DATA_ERR;
                            msg = "数据错误";
                        }
                    }
                }
                if(callback != null)
                    callback.onFinish(code, msg);
            }
        }).execute(this);
    }

    // 下载并安装apk文件
    public void downloadApkFileAndInstall(Context context) {
        File apkFile = new File(DIR_CACHE, "kmic.apk");
        UploadDownloadFileUtil.downloadFile(context, url, apkFile, new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                installApk(context, apkFile);
            }
        });
    }

    private void installApk(Context context, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", apkFile);
        } else {
            uri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    @NonNull
    @Override
    public String toString() {
        return "verCode:" + verCode + " verName:" + verName + " note:" + note + " size:" + size + " url:" + url;
    }
}
