package com.cmtech.android.bledeviceapp.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_CACHE;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      AppUpdateInfo
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/10/28 上午6:28
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/28 上午6:28
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class AppUpdateInfo implements Serializable, IJsonable, IWebOperation {
    private int verCode;
    private String verName;
    private String note;
    private String url;
    private double size; // unit: MB

    public AppUpdateInfo() {

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
        return null;
    }

    @Override
    public void upload(Context context, IWebCallback callback) {

    }

    @Override
    public void download(Context context, IWebCallback callback) {
        new AppUpdateInfoWebAsyncTask(new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if (code == RETURN_CODE_SUCCESS) {
                    JSONObject json = (JSONObject) result;
                    if(json != null) {
                        try {
                            fromJson(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = RETURN_CODE_OTHER_ERR;
                        }
                    }
                }
                callback.onFinish(code, result);
            }
        }).execute();
    }

    @Override
    public void delete(Context context, IWebCallback callback) {

    }

    @Override
    public void retrieveList(Context context, int num, String queryStr, long fromTime, IWebCallback callback) {

    }

    /**
     * 下载最新版本的apk
     *
     */
    public void downApkFile(Context context) {
        if(TextUtils.isEmpty(url)) return;

        ProgressDialog pBar = new ProgressDialog(context);
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pBar.setCancelable(false);
        pBar.setTitle("正在下载...");
        pBar.setMessage("请稍候...");
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
                            //对apk进行保存
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

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
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
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider", file);
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

    private static class AppUpdateInfoWebAsyncTask extends AsyncTask<Void, Void, Object[]> {
        private static final int WAIT_TASK_SECOND = 10;

        private IWebCallback callback;

        public AppUpdateInfoWebAsyncTask(IWebCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Object[] doInBackground(Void... voids) {
            final Object[] result = {RETURN_CODE_WEB_FAILURE, null};

            CountDownLatch done = new CountDownLatch(1);
            KMWebServiceUtil.downloadAppUpdateInfo(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    done.countDown();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String respBody = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject json = new JSONObject(respBody);
                        //ViseLog.e(json);
                        result[0] = json.getInt("code");
                        result[1] = json.getJSONObject("appUpdateInfo");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        done.countDown();
                    }
                }
            });


            try {
                done.await(WAIT_TASK_SECOND, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] result) {
            callback.onFinish((Integer) result[0], result[1]);
        }
    }
}
