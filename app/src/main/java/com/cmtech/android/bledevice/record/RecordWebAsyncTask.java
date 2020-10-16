package com.cmtech.android.bledevice.record;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.*;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.record
 * ClassName:      RecordWebAsyncTask
 * Description:    执行记录的网络操作的AsyncTask类
 * Author:         作者名
 * CreateDate:     2020/4/20 上午6:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/20 上午6:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class RecordWebAsyncTask extends AsyncTask<BasicRecord, Void, Object[]> {
    public static final int RECORD_CMD_UPLOAD = 1; // upload record command
    public static final int RECORD_CMD_UPDATE = 2; // update record info
    public static final int RECORD_CMD_QUERY_ID = 3; // query record command ID
    public static final int RECORD_CMD_DELETE = 4; // delete record command
    public static final int RECORD_CMD_DOWNLOAD_LIST = 5; // download basic record information command
    public static final int RECORD_CMD_DOWNLOAD = 6; // download record command

    private static final int WAIT_TASK_FINISH_TIME = 10; // unit: second
    private static final int DEFAULT_DOWNLOAD_NUM = 10;

    private ProgressDialog progressDialog;
    private final int cmd;
    private final boolean showProgress;
    private final Object[] params;
    private final IWebCallback callback;

    public RecordWebAsyncTask(Context context, int cmd, IWebCallback callback) {
        this(context, cmd, true, callback);
    }

    public RecordWebAsyncTask(Context context, int cmd, boolean showProgress, IWebCallback callback) {
        this(context, cmd, null, showProgress, callback);
    }

    public RecordWebAsyncTask(Context context, int cmd, Object[] params, IWebCallback callback) {
        this(context, cmd, params, true, callback);
    }

    public RecordWebAsyncTask(Context context, int cmd, Object[] params, boolean showProgress, IWebCallback callback) {
        this.cmd = cmd;
        this.params = params;
        this.showProgress = showProgress;
        this.callback = callback;

        if(showProgress) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getResources().getString(R.string.wait_pls));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
    }

    @Override
    protected void onPreExecute() {
        if(showProgress)
            progressDialog.show();
    }

    @Override
    protected Object[] doInBackground(BasicRecord... records) {
        if(records == null || records.length == 0) return null;

        BasicRecord record = records[0];
        CountDownLatch done = new CountDownLatch(1);
        final Object[] result = {RETURN_CODE_WEB_FAILURE, null};

        switch (cmd) {
            // QUERY
            case RECORD_CMD_QUERY_ID:
                KMWebServiceUtil.queryRecordId(record.getTypeCode(), record.getCreateTime(), record.getDevAddress(), record.getVer(), new Callback() {
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
                            if((Integer) result[0] == RETURN_CODE_SUCCESS) {
                                int id = json.getInt("id");
                                result[1] = id;
                                ViseLog.e("Find Record id = " + id);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            result[0] = RETURN_CODE_OTHER_ERR;
                        } finally {
                            done.countDown();
                        }
                    }
                });
                break;

            // UPLOAD
            case RECORD_CMD_UPLOAD:
                KMWebServiceUtil.uploadRecord(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, new Callback() {
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
                            result[0] = RETURN_CODE_OTHER_ERR;
                        } finally {
                            done.countDown();
                        }
                    }
                });
                break;

            // UPDATE
            case RECORD_CMD_UPDATE:
                KMWebServiceUtil.updateRecord(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, new Callback() {
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
                            result[0] = RETURN_CODE_OTHER_ERR;
                        } finally {
                            done.countDown();
                        }
                    }
                });
                break;

            // DOWNLOAD
            case RECORD_CMD_DOWNLOAD:
                KMWebServiceUtil.downloadRecord(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, new Callback() {
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
                            if((Integer) result[0] == RETURN_CODE_SUCCESS) {
                                result[1] = json.get("record");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            result[0] = RETURN_CODE_OTHER_ERR;
                        } finally {
                            done.countDown();
                        }
                    }
                });
                break;

            // DELETE
            case RECORD_CMD_DELETE:
                KMWebServiceUtil.deleteRecord(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, new Callback() {
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
                            result[0] = RETURN_CODE_OTHER_ERR;
                        } finally {
                            done.countDown();
                        }
                    }
                });
                break;

            // DOWNLOAD BASIC INFO
            case RECORD_CMD_DOWNLOAD_LIST:
                int downloadNum = 0;
                String noteSearchStr = "";
                long fromTime = new Date().getTime();
                if(params == null || params.length == 0) {
                    downloadNum = DEFAULT_DOWNLOAD_NUM;
                } else if(params.length == 1) {
                    downloadNum = (Integer) params[0];
                } else if(params.length == 2) {
                    downloadNum = (Integer) params[0];
                    noteSearchStr = (String) params[1];
                } else if(params.length == 3) {
                    downloadNum = (Integer) params[0];
                    noteSearchStr = (String) params[1];
                    fromTime = (Long) params[2];
                }
                KMWebServiceUtil.downloadRecordList(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, fromTime, downloadNum, noteSearchStr, new Callback() {
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
                            result[1] = json.get("records");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            result[0] = RETURN_CODE_OTHER_ERR;
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
            done.await(WAIT_TASK_FINISH_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(Object[] result) {
        callback.onFinish((Integer) result[0], result[1]);
        if(showProgress)
            progressDialog.dismiss();
    }
}
