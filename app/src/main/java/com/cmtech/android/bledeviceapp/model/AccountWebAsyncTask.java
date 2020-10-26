package com.cmtech.android.bledeviceapp.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;

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
public class AccountWebAsyncTask extends AsyncTask<Account, Void, Object[]> {
    public static final int UPLOAD_CMD = 1; // upload user info command
    public static final int DOWNLOAD_CMD = 2; // download user info command

    private static final int WAIT_TASK_SECOND = 10; // time to wait for task finish

    private final ProgressDialog progressDialog;
    private final int cmd;
    private final IWebCallback callback;

    public AccountWebAsyncTask(Context context, int cmd, boolean showProgress, IWebCallback callback) {
        this.cmd = cmd;
        this.callback = callback;

        if(showProgress) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getResources().getString(R.string.wait_pls));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        } else {
            progressDialog = null;
        }
    }

    @Override
    protected void onPreExecute() {
        if(progressDialog != null)
            progressDialog.show();
    }

    @Override
    protected Object[] doInBackground(Account... accounts) {
        final Object[] result = {RETURN_CODE_WEB_FAILURE, null};

        if(accounts == null || accounts.length == 0 || accounts[0] == null) return result;

        Account account = accounts[0];
        CountDownLatch done = new CountDownLatch(1);

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
                        String respBody = Objects.requireNonNull(response.body()).string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            result[0] = json.getInt("code");
                        } catch (JSONException e) {
                            e.printStackTrace();
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
                        String respBody = Objects.requireNonNull(response.body()).string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            result[0] = json.getInt("code");
                            result[1] = json.get("account");
                        } catch (JSONException e) {
                            e.printStackTrace();
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
        if(progressDialog != null)
            progressDialog.dismiss();
        int code = (int)result[0];
        callback.onFinish(code, result[1]);
    }
}
