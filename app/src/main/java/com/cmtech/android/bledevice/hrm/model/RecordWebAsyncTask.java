package com.cmtech.android.bledevice.hrm.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledevice.interf.AbstractRecord;
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
 * Package:        com.cmtech.android.bledevice.hrm.model
 * ClassName:      RecordOpAsyncTask
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/20 上午6:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/20 上午6:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class RecordWebAsyncTask extends AsyncTask<AbstractRecord, Void, Void> {
    public static final int RECORD_UPLOAD_CMD = 1;
    public static final int RECORD_UPDATE_NOTE_CMD = 2;
    public static final int RECORD_QUERY_CMD = 3;
    public static final int RECORD_DELETE_CMD = 4;
    public static final int RECORD_DOWNLOAD_CMD = 5;

    private static final int DOWNLOAD_NUM_PER_TIME = 20;

    private ProgressDialog progressDialog;
    private final int cmd;
    private String cmdStr = "未知命令";
    private final boolean isShowProgress;

    private boolean finish = false;

    private int code;
    private String errStr;
    private Object rlt;

    public interface RecordWebCallback {
        void onFinish(Object[] objs);
    }

    private final RecordWebCallback callback;

    public RecordWebAsyncTask(Context context, int cmd, RecordWebCallback callback) {
        this(context, cmd, true, callback);
    }

    public RecordWebAsyncTask(Context context, int cmd, boolean isShowProgress, RecordWebCallback callback) {
        this.cmd = cmd;

        switch (cmd) {
            case RECORD_UPLOAD_CMD:
                cmdStr = "上传记录";
                break;
            case RECORD_UPDATE_NOTE_CMD:
                cmdStr = "更新记录";
                break;
            case RECORD_QUERY_CMD:
                cmdStr = "查询记录";
                break;
            case RECORD_DELETE_CMD:
                cmdStr = "删除记录";
                break;
            case RECORD_DOWNLOAD_CMD:
                cmdStr = "下载记录";
                break;
            default:
                break;
        }

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(cmdStr);
        progressDialog.setMessage(cmdStr+"中，请稍等...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        this.callback = callback;

        this.isShowProgress = isShowProgress;
    }

    @Override
    protected void onPreExecute() {
        if(isShowProgress)
            progressDialog.show();
    }

    @Override
    protected Void doInBackground(AbstractRecord... records) {
        if(records == null) return null;

        AbstractRecord record = records[0];

        switch (cmd) {
            case RECORD_UPLOAD_CMD:
                KMWebService.uploadRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), (BleEcgRecord10) record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            errStr = json.getString("errStr");
                            ViseLog.e(code+errStr);
                            finish = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;

            case RECORD_UPDATE_NOTE_CMD:
                KMWebService.updateRecordNote(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), (BleEcgRecord10) record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            errStr = json.getString("errStr");
                            ViseLog.e(code+errStr);
                            finish = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;

            case RECORD_QUERY_CMD:
                KMWebService.queryRecord(1, record.getCreateTime(), record.getDevAddress(), new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if(response.body() == null) return;
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            int id = json.getInt("id");
                            ViseLog.e("find ecg record id = " + id);
                            code = 0;
                            errStr = "查询成功";
                            rlt = (Integer)id;
                            finish = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;

            case RECORD_DELETE_CMD:
                KMWebService.deleteRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), (BleEcgRecord10) record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            errStr = json.getString("errStr");
                            ViseLog.e(code+errStr);
                            finish = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;

            case RECORD_DOWNLOAD_CMD:
                KMWebService.downloadRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), (BleEcgRecord10)record, DOWNLOAD_NUM_PER_TIME, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            errStr = json.getString("errStr");
                            rlt = json;
                            ViseLog.e(json.toString());
                            finish = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;

            default:
                code = 1;
                errStr = "无效命令";
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
        progressDialog.dismiss();

        callback.onFinish(new Object[]{code, errStr, rlt});
    }
}
