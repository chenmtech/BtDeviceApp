package com.cmtech.android.bledevice.record;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.KMWebService;
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
 * Package:        com.cmtech.android.bledevice.record
 * ClassName:      RecordWebAsyncTask
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/20 上午6:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/20 上午6:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class RecordWebAsyncTask extends AsyncTask<IRecord, Void, Void> {
    public static final int RECORD_UPLOAD_CMD = 1; // upload record command
    public static final int RECORD_UPDATE_NOTE_CMD = 2; // update record note command
    public static final int RECORD_QUERY_CMD = 3; // query record command
    public static final int RECORD_DELETE_CMD = 4; // delete record command
    public static final int RECORD_INFO_DOWNLOAD_CMD = 5; // download record information command
    public static final int RECORD_DOWNLOAD_CMD = 6; // download record command

    public static final int CODE_SUCCESS = 0; // success
    private static final int DEFAULT_DOWNLOAD_NUM_PER_TIME = 10;

    public interface RecordWebCallback {
        void onFinish(int code, String desc, Object result);
    }

    private ProgressDialog progressDialog;
    private final int cmd;
    private final boolean isShowProgress;
    private final Object param;
    private final RecordWebCallback callback;

    private int code;
    private String desc;
    private Object rlt;
    private boolean finish = false;

    public RecordWebAsyncTask(Context context, int cmd, RecordWebCallback callback) {
        this(context, cmd, true, callback);
    }

    public RecordWebAsyncTask(Context context, int cmd, boolean isShowProgress, RecordWebCallback callback) {
        this(context, cmd, null, isShowProgress, callback);
    }

    public RecordWebAsyncTask(Context context, int cmd, Object param, boolean isShowProgress, RecordWebCallback callback) {
        this.cmd = cmd;
        this.param = param;

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
    protected Void doInBackground(IRecord... records) {
        if(records == null) return null;

        IRecord record = records[0];

        switch (cmd) {
            // QUERY
            case RECORD_QUERY_CMD:
                KMWebService.queryRecord(record.getTypeCode(), record.getCreateTime(), record.getDevAddress(), new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        desc = "网络错误";
                        rlt = null;
                        ViseLog.e(code+ desc);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if(response.body() == null) return;
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            int id = json.getInt("id");
                            ViseLog.e("find ecg record id = " + id);
                            code = CODE_SUCCESS;
                            desc = "查询成功";
                            rlt = (Integer)id;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            desc = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // UPLOAD
            case RECORD_UPLOAD_CMD:
                KMWebService.uploadRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        desc = "网络错误";
                        rlt = null;
                        ViseLog.e(code+ desc);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            desc = json.getString("errStr");
                            ViseLog.e(code+ desc);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            desc = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // UPDATE NOTE
            case RECORD_UPDATE_NOTE_CMD:
                KMWebService.updateRecordNote(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        desc = "网络错误";
                        rlt = null;
                        ViseLog.e(code+ desc);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            desc = json.getString("errStr");
                            ViseLog.e(code+ desc);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            desc = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // DELETE
            case RECORD_DELETE_CMD:
                KMWebService.deleteRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        desc = "网络错误";
                        rlt = null;
                        ViseLog.e(code+ desc);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            desc = json.getString("errStr");
                            ViseLog.e(code+ desc);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            desc = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // DOWNLOAD INFO
            case RECORD_INFO_DOWNLOAD_CMD:
                int downloadNum = (param == null) ? DEFAULT_DOWNLOAD_NUM_PER_TIME : (Integer)param;
                KMWebService.downloadRecordInfo(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, downloadNum, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        desc = "网络错误";
                        rlt = null;
                        ViseLog.e(code+ desc);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            desc = json.getString("errStr");
                            rlt = json.get("records");
                            ViseLog.e(json.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            desc = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // DOWNLOAD
            case RECORD_DOWNLOAD_CMD:
                KMWebService.downloadRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        desc = "网络错误";
                        rlt = null;
                        ViseLog.e(code+ desc);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            ViseLog.e(json.toString());
                            code = json.getInt("code");
                            desc = json.getString("errStr");
                            rlt = json.get("record");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            desc = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            default:
                code = 1;
                desc = "无效命令";
                rlt = null;
                finish = true;
                break;
        }

        while (!finish) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        callback.onFinish(code, desc, rlt);
        progressDialog.dismiss();
    }
}
