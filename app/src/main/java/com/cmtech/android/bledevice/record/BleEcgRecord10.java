package com.cmtech.android.bledevice.record;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.cmtech.android.bledevice.report.EcgReport;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.interfac.IWebOperationCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledevice.record.RecordType.ECG;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.RECORD_CMD_DOWNLOAD;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_FAILURE;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_SUCCESS;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.record
 * ClassName:      BleEcgRecord10
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午7:11
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleEcgRecord10 extends BasicRecord implements ISignalRecord, Serializable, IWebOperation {
    public static final int REPORT_CMD_REQUEST = 0; // upload record command
    public static final int REPORT_CMD_GET_NEW = 1; // update record note command

    private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private int recordSecond; // unit: s
    private List<Short> ecgData; // ecg data
    private EcgReport report;
    @Column(ignore = true)
    private int pos = 0; // current position to the ecgData

    private BleEcgRecord10() {
        super(ECG);
        initData();
        recordSecond = 0;
    }

    private BleEcgRecord10(long createTime, String devAddress, Account creator, String note) {
        super(ECG, "1.0", createTime, devAddress, creator, note, true);
        initData();
        recordSecond = 0;
    }

    private BleEcgRecord10(JSONObject json) throws JSONException{
        super(json, false);
        initData();
        recordSecond = json.getInt("recordSecond");
    }

    private void initData() {
        sampleRate = 0;
        caliValue = 0;
        leadTypeCode = 0;
        ecgData = new ArrayList<>();
        report = new EcgReport();
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("caliValue", caliValue);
        json.put("leadTypeCode", leadTypeCode);
        json.put("recordSecond", recordSecond);
        StringBuilder builder = new StringBuilder();
        for(Short ele : ecgData) {
            builder.append(ele).append(',');
        }
        json.put("ecgData", builder.toString());
        return json;
    }

    @Override
    public boolean fromJson(JSONObject json) throws JSONException{
        if(json == null) {
            return false;
        }

        sampleRate = json.getInt("sampleRate");
        caliValue = json.getInt("caliValue");
        leadTypeCode = json.getInt("leadTypeCode");
        recordSecond = json.getInt("recordSecond");
        String ecgDataStr = json.getString("ecgData");
        List<Short> ecgData = new ArrayList<>();
        String[] strings = ecgDataStr.split(",");
        for(String str : strings) {
            ecgData.add(Short.parseShort(str));
        }
        this.ecgData = ecgData;
        return save();
    }

    @Override
    public boolean noSignal() {
        return ecgData.isEmpty();
    }

    public List<Short> getEcgData() {
        return ecgData;
    }

    public void setEcgData(List<Short> ecgData) {
        this.ecgData.addAll(ecgData);
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public int getCaliValue() {
        return caliValue;
    }

    public void setCaliValue(int caliValue) {
        this.caliValue = caliValue;
    }

    public void setLeadTypeCode(int leadTypeCode) {
        this.leadTypeCode = leadTypeCode;
    }

    public EcgReport getReport() {
        return report;
    }

    public void setReport(EcgReport report) {
        this.report = report;
    }

    @Override
    public boolean isEOD() {
        return (pos >= ecgData.size());
    }

    @Override
    public void seekData(int pos) {
        this.pos = pos;
    }

    @Override
    public int readData() throws IOException {
        if(pos >= ecgData.size()) throw new IOException();
        return ecgData.get(pos++);
    }

    @Override
    public int getDataNum() {
        return ecgData.size();
    }

    @Override
    public int getRecordSecond() {
        return recordSecond;
    }

    public void setRecordSecond(int recordSecond) {
        this.recordSecond = recordSecond;
    }

    public boolean process(short ecg) {
        ecgData.add(ecg);
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + leadTypeCode + "-" + recordSecond + "-" + ecgData + "-" + report;
    }

    public void requestReport(Context context, int cmd, IWebOperationCallback callback) {
        if(needUpload()) {
            upload(context, new IWebOperationCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    if(code == SUCCESS) {
                        doRequestRequest(context, cmd, callback);
                    } else {
                        Toast.makeText(context, R.string.web_failure, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            doRequestRequest(context, cmd, callback);
        }
    }

    private void doRequestRequest(Context context, int cmd, IWebOperationCallback callback) {
        new ReportWebAsyncTask(context, cmd, callback).execute(this);
    }

    private static class ReportWebAsyncTask extends AsyncTask<BleEcgRecord10, Void, Object[]> {
        private static final int WAIT_TASK_SECOND = 10;

        private IWebOperationCallback callback;
        private ProgressDialog progressDialog;
        private int cmd;

        public ReportWebAsyncTask(Context context, int cmd, IWebOperationCallback callback) {
            this.callback = callback;
            this.cmd = cmd;
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
        protected Object[] doInBackground(BleEcgRecord10... ecgRecords) {
            final Object[] result = {WEB_CODE_FAILURE, null};
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

            if(cmd == REPORT_CMD_REQUEST)
                KMWebServiceUtil.requestReport(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, callback);
            else
                KMWebServiceUtil.getNewReport(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, callback);

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
}
