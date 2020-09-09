package com.cmtech.android.bledevice.report;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledevice.record.IRecordWebCallback;
import com.cmtech.android.bledevice.record.RecordWebAsyncTask;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledevice.record.IRecord.INVALID_ID;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_FAILURE;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_SUCCESS;

public class EcgReport extends LitePalSupport implements IJsonable {
    private int id;
    @Column(ignore = true)
    private final BleEcgRecord10 record;
    private String ver = "1.0";
    private long createTime = -1;
    private String content = "";

    public EcgReport(BleEcgRecord10 record) {
        this.record = record;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean fromJson(JSONObject json) {
        if(json == null) {
            return false;
        }

        try {
            ver = json.getString("ver");
            createTime = json.getLong("createTime");
            content = json.getString("content");
            return save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("recordCreateTime", record.getCreateTime());
        json.put("recordDevAddress", record.getDevAddress());
        json.put("ver", ver);
        json.put("createTime", createTime);
        json.put("content", content);
        return json;
    }

    public void request(Context context, IReportWebCallback callback) {
        if(record.needUpload()) {
            new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_QUERY, (code, rlt) -> {
                final boolean result = (code == WEB_CODE_SUCCESS);
                if (result) {
                    int id = (Integer) rlt;
                    if (id == INVALID_ID) {
                        ViseLog.e("uploading");
                        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_UPLOAD, false, new IRecordWebCallback() {
                            @Override
                            public void onFinish(int code, Object result) {
                                if (code == WEB_CODE_SUCCESS) {
                                    record.setNeedUpload(false);
                                    record.save();
                                    doRequest(context, callback);
                                } else {
                                    Toast.makeText(context, R.string.web_failure, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).execute(record);
                    } else {
                        ViseLog.e("updating note");
                        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_UPDATE_NOTE, false, new IRecordWebCallback() {
                            @Override
                            public void onFinish(int code, Object result) {
                                if (code == WEB_CODE_SUCCESS) {
                                    record.setNeedUpload(false);
                                    record.save();
                                    doRequest(context, callback);
                                } else {
                                    Toast.makeText(context, R.string.web_failure, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).execute(record);
                    }
                } else {
                    Toast.makeText(context, R.string.web_failure, Toast.LENGTH_SHORT).show();
                }
            }).execute(record);
        } else {
            doRequest(context, callback);
        }
    }

    private void doRequest(Context context, IReportWebCallback callback) {
        new ReportWebAsyncTask(context, callback).execute(this);
    }

    private static class ReportWebAsyncTask extends AsyncTask<EcgReport, Void, Object[]> {
        private static final int WAIT_TASK_SECOND = 10;

        private IReportWebCallback callback;
        private ProgressDialog progressDialog;

        public ReportWebAsyncTask(Context context, IReportWebCallback callback) {
            this.callback = callback;

            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getResources().getString(R.string.wait_pls));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Object[] doInBackground(EcgReport... ecgReports) {
            final Object[] result = {WEB_CODE_FAILURE, null};
            if(ecgReports == null || ecgReports.length == 0 || ecgReports[0] == null) return result;

            EcgReport report = ecgReports[0];
            CountDownLatch done = new CountDownLatch(1);

            KMWebServiceUtil.requestReport(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), report, new Callback() {
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
            progressDialog.dismiss();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "报告创建时间：" + new Date(createTime) + "内容：" + content;
    }
}
