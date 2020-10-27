package com.cmtech.android.bledeviceapp.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledevice.record.BasicRecord.INVALID_ID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_IMAGE;

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
    private int id = INVALID_ID;
    private int verCode;
    private String verName;
    private String note;
    private String url;

    public AppUpdateInfo() {

    }

    @Override
    public void fromJson(JSONObject json) throws JSONException {
        try {
            verCode = json.getInt("verCode");
            verName = json.getString("verName");
            note = json.getString("note");
            url = json.getString("url");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
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

    }

    @Override
    public void delete(Context context, IWebCallback callback) {

    }

    @Override
    public void retrieveList(Context context, int num, String queryStr, long fromTime, IWebCallback callback) {

    }

    private static class AppUpdateInfoWebAsyncTask extends AsyncTask<Void, Void, Void> {
        private static final int WAIT_TASK_SECOND = 10;

        private IWebCallback callback;

        public AppUpdateInfoWebAsyncTask(IWebCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final Object[] result = {RETURN_CODE_WEB_FAILURE, null};
            if(ecgRecords == null || ecgRecords.length == 0 || ecgRecords[0] == null) return result;

            BleEcgRecord10 record = ecgRecords[0];
            CountDownLatch done = new CountDownLatch(1);

            Callback callback = new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    done.countDown();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String respBody = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject json = new JSONObject(respBody);
                        result[0] = json.getInt("code");
                        if(json.has("reportResult"))
                            result[1] = json.getJSONObject("reportResult");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        done.countDown();
                    }
                }
            };

            switch (cmd) {
                case REPORT_CMD_REQUEST:
                    KMWebServiceUtil.requestReport(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, callback);
                    break;
                case REPORT_CMD_DOWNLOAD:
                    KMWebServiceUtil.downloadReport(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, callback);
                    break;
                default:
                    done.countDown();
                    break;
            }
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
