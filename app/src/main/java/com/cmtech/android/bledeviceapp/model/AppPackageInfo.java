package com.cmtech.android.bledeviceapp.model;

import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_CACHE;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.cmtech.android.bledeviceapp.asynctask.AppInfoWebAsyncTask;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.util.ThreadUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      AppPackageInfo
 * Description:    应用程序安装包信息
 * Author:         作者名
 * CreateDate:     2020/10/28 上午6:28
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/28 上午6:28
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class AppPackageInfo implements Serializable, IJsonable, IWebOperation {
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

    @Override
    public void upload(Context context, ICodeCallback callback) {
        throw new IllegalStateException("Cannot use upload of AppPackageInfo");
    }

    @Override
    public void download(Context context, String showStr, ICodeCallback callback) {
        new AppInfoWebAsyncTask(context, AppInfoWebAsyncTask.CMD_DOWNLOAD_INFO, (response) -> {
            int code = response.getCode();
            if (code == RETURN_CODE_SUCCESS) {
                JSONObject json = (JSONObject) response.getContent();
                if(json != null) {
                    try {
                        fromJson(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        code = RETURN_CODE_DATA_ERR;
                    }
                }
            }
            callback.onFinish(code);
        }).execute(this);
    }

    @Override
    public void delete(Context context, ICodeCallback callback) {
        throw new IllegalStateException("Cannot use delete of AppPackageInfo");
    }

    public void downloadApkFileAndInstall(Context context) {
        if(TextUtils.isEmpty(url)) return;

        ProgressDialog pBar = new ProgressDialog(context);
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pBar.setCancelable(false);
        pBar.setTitle("下载安装包");
        pBar.setMessage("正在下载安装包，请稍候...");
        pBar.setProgress(0);
        pBar.show();
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(getUrl());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setReadTimeout(5000);
                    con.setConnectTimeout(5000);
                    con.setRequestProperty("Charset", "UTF-8");
                    con.setRequestMethod("GET");
                    if (con.getResponseCode() == 200) {
                        int length = con.getContentLength();// 获取文件大小
                        InputStream is = con.getInputStream();
                        pBar.setMax(100); // 设置进度条的总长度
                        FileOutputStream fileOutputStream = null;
                        File file = null;
                        if (is != null) {
                            //将apk文件下载到DIR_CACHE文件夹中
                            file = new File(DIR_CACHE, "kmic.apk");
                            fileOutputStream = new FileOutputStream(file);
                            byte[] buf = new byte[1024];
                            int ch;
                            int process = 0;
                            while ((ch = is.read(buf)) != -1) {
                                fileOutputStream.write(buf, 0, ch);
                                process += ch;
                                pBar.setProgress((int)(process*100.0/length)); // 实时更新进度了
                            }
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        }

                        ThreadUtil.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pBar.dismiss();
                                installApk(context);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void installApk(Context context) {
        File file = new File(DIR_CACHE, "kmic.apk");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
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
