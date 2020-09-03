package com.cmtech.android.bledeviceapp.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_FAILURE;

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
public class AccountInfoWebAsyncTask extends AsyncTask<Account, Void, Object[]> {
    public static final int UPLOAD_CMD = 1; // upload user info command
    public static final int DOWNLOAD_CMD = 2; // download user info command

    private static final int WAIT_TASK_SECOND = 10;

    private final ProgressDialog progressDialog;
    private final int cmd;
    private final AccountInfoWebCallback callback;

    public AccountInfoWebAsyncTask(Context context, int cmd, AccountInfoWebCallback callback) {
        this(context, cmd, true, callback);
    }

    public AccountInfoWebAsyncTask(int cmd, AccountInfoWebCallback callback) {
        this(null, cmd, false, callback);
    }

    private AccountInfoWebAsyncTask(Context context, int cmd, boolean isShowProgress, AccountInfoWebCallback callback) {
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
    protected Object[] doInBackground(Account... accounts) {
        if(accounts == null || accounts.length == 0) return null;

        Account account = accounts[0];
        CountDownLatch done = new CountDownLatch(1);
        final Object[] result = {WEB_CODE_FAILURE, null};

        switch (cmd) {
            // UPLOAD
            case UPLOAD_CMD:
                KMWebServiceUtil.uploadAccountInfo(account, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        done.countDown();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            result[0] = json.getInt("code");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            result[0] = WEB_CODE_FAILURE;
                            result[1] = null;
                        } finally {
                            done.countDown();
                        }
                    }
                });
                break;

            // DOWNLOAD
            case DOWNLOAD_CMD:
                KMWebServiceUtil.downloadAccountInfo(account, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        done.countDown();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            ViseLog.e(json.toString());
                            result[0] = json.getInt("code");
                            result[1] = json.get("account");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            result[0] = WEB_CODE_FAILURE;
                            result[1] = null;
                        } finally {
                            done.countDown();
                        }
                    }
                });
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
        if(progressDialog != null)
            progressDialog.dismiss();
    }
}
