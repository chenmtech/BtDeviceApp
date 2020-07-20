package com.cmtech.android.bledeviceapp.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

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
public class UserInfoWebAsyncTask extends AsyncTask<User, Void, Void> {
    public static final int UPLOAD_CMD = 1; // upload user info command
    public static final int DOWNLOAD_CMD = 2; // download user info command

    public static final int CODE_SUCCESS = 0; // success
    public static final int CODE_FAILURE = 1; // failure

    public static final int WAIT_TASK_SECOND = 10;

    public interface UserInfoWebCallback {
        void onFinish(int code, Object result);
    }

    private final ProgressDialog progressDialog;
    private final int cmd;
    private final UserInfoWebCallback callback;

    private int code = CODE_FAILURE;
    private Object rlt = null;
    private boolean taskFinish = false;

    public UserInfoWebAsyncTask(Context context, int cmd, UserInfoWebCallback callback) {
        this(context, cmd, true, callback);
    }

    public UserInfoWebAsyncTask(int cmd, UserInfoWebCallback callback) {
        this(null, cmd, false, callback);
    }

    private UserInfoWebAsyncTask(Context context, int cmd, boolean isShowProgress, UserInfoWebCallback callback) {
        this.cmd = cmd;

        if(isShowProgress) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getResources().getString(R.string.wait_pls));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        } else {
            progressDialog = null;
        }
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        if(progressDialog != null)
            progressDialog.show();
    }

    @Override
    protected Void doInBackground(User... users) {
        if(users == null) return null;

        User user = users[0];

        switch (cmd) {
            // UPLOAD
            case UPLOAD_CMD:
                KMWebService.uploadUserInfo(user, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = CODE_FAILURE;
                        rlt = null;
                        taskFinish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = CODE_FAILURE;
                            rlt = null;
                        } finally {
                            taskFinish = true;
                        }
                    }
                });
                break;

            // DOWNLOAD
            case DOWNLOAD_CMD:
                KMWebService.downloadUserInfo(user, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = CODE_FAILURE;
                        rlt = null;
                        taskFinish = true;
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
                            code = CODE_FAILURE;
                            rlt = null;
                        } finally {
                            taskFinish = true;
                        }
                    }
                });
                break;

            default:
                code = CODE_FAILURE;
                rlt = null;
                taskFinish = true;
                break;
        }

        int i = 0;
        while (!taskFinish && i++ < WAIT_TASK_SECOND) {
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

        if(progressDialog != null)
            progressDialog.dismiss();
    }
}
