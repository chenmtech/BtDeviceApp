package com.cmtech.android.bledevice.record;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
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
    public static final int RECORD_CMD_UPLOAD = 1; // upload record command
    public static final int RECORD_CMD_UPDATE_NOTE = 2; // update record note command
    public static final int RECORD_CMD_QUERY = 3; // query record command
    public static final int RECORD_CMD_DELETE = 4; // delete record command
    public static final int RECORD_CMD_DOWNLOAD_BASIC_INFO = 5; // download basic record information command
    public static final int RECORD_CMD_DOWNLOAD = 6; // download record command

    private static final int DEFAULT_DOWNLOAD_BASIC_INFO_NUM_PER_TIME = 10;

    public interface RecordWebCallback {
        void onFinish(int code, Object result);
    }

    private ProgressDialog progressDialog;
    private final int cmd;
    private final boolean isShowProgress;
    private final Object[] params;
    private final RecordWebCallback callback;

    private int code = 1;
    private Object rlt = null;
    private boolean finish = false;

    public RecordWebAsyncTask(Context context, int cmd, RecordWebCallback callback) {
        this(context, cmd, true, callback);
    }

    public RecordWebAsyncTask(Context context, int cmd, boolean isShowProgress, RecordWebCallback callback) {
        this(context, cmd, null, isShowProgress, callback);
    }

    public RecordWebAsyncTask(Context context, int cmd, Object[] params, boolean isShowProgress, RecordWebCallback callback) {
        this.cmd = cmd;
        this.params = params;

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
            case RECORD_CMD_QUERY:
                KMWebService.queryRecord(record.getTypeCode(), record.getCreateTime(), record.getDevAddress(), new Callback() {
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
                            int id = json.getInt("id");
                            ViseLog.e("code=" + code + ", find record id = " + id);
                            rlt = id;
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

            // UPLOAD
            case RECORD_CMD_UPLOAD:
                KMWebService.uploadRecord(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, new Callback() {
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

            // UPDATE NOTE
            case RECORD_CMD_UPDATE_NOTE:
                KMWebService.updateRecordNote(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, new Callback() {
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

            // DELETE
            case RECORD_CMD_DELETE:
                KMWebService.deleteRecord(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, new Callback() {
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

            // DOWNLOAD BASIC INFO
            case RECORD_CMD_DOWNLOAD_BASIC_INFO:
                int downloadNum = 0;
                String noteFilterStr = "";
                if(params == null || params.length == 0) {
                    downloadNum = DEFAULT_DOWNLOAD_BASIC_INFO_NUM_PER_TIME;
                } else if(params.length == 1) {
                    downloadNum = (Integer) params[0];
                } else if(params.length == 2) {
                    downloadNum = (Integer) params[0];
                    noteFilterStr = (String) params[1];
                }
                KMWebService.downloadRecordBasicInfo(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, downloadNum, noteFilterStr, new Callback() {
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
                            rlt = json.get("records");
                            //ViseLog.e(json.toString());
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
            case RECORD_CMD_DOWNLOAD:
                KMWebService.downloadRecord(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, new Callback() {
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
                            rlt = json.get("record");
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
