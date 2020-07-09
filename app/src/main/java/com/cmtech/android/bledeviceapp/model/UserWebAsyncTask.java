package com.cmtech.android.bledeviceapp.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      UserWebAsyncTask
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/20 上午6:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/20 上午6:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class UserWebAsyncTask extends AsyncTask<User, Void, Void> {
    public static final int USER_CMD_UPLOAD = 1; // upload user command
    public static final int USER_CMD_DOWNLOAD = 2; // download user command

    public static final int CODE_SUCCESS = 0; // success

    public interface UserWebCallback {
        void onFinish(int code, Object result);
    }

    private ProgressDialog progressDialog;
    private final int cmd;
    private final boolean isShowProgress;
    private final UserWebCallback callback;

    private int code = 1;
    private Object rlt = null;
    private boolean finish = false;

    public UserWebAsyncTask(Context context, int cmd, UserWebCallback callback) {
        this(context, cmd, true, callback);
    }

    public UserWebAsyncTask(Context context, int cmd, boolean isShowProgress, UserWebCallback callback) {
        this.cmd = cmd;

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.wait_pls));
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        this.isShowProgress = isShowProgress;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        if(isShowProgress)
            progressDialog.show();
    }

    @Override
    protected Void doInBackground(User... users) {
        if(users == null) return null;

        User user = users[0];

        switch (cmd) {
            // UPLOAD
            case USER_CMD_UPLOAD:
                KMWebService.uploadUser(user, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        rlt = null;
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // DOWNLOAD
            case USER_CMD_DOWNLOAD:
                KMWebService.downloadUser(user, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        rlt = null;
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            ViseLog.e(json.toString());
                            code = json.getInt("code");
                            rlt = json.get("user");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            default:
                code = 1;
                rlt = null;
                finish = true;
                break;
        }

        int i = 0;
        while (!finish && i++ < 10) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        callback.onFinish(code, rlt);
        progressDialog.dismiss();
    }
}
