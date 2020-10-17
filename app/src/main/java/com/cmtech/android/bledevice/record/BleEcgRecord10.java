package com.cmtech.android.bledevice.record;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.cmtech.android.bledevice.report.EcgReport;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.ecgprocess.EcgProcessor;
import com.cmtech.android.bledeviceapp.ecgprocess.afdetector.AFEvidence.MyAFEvidence;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledevice.record.RecordType.ECG;
import static com.cmtech.android.bledeviceapp.ecgprocess.afdetector.AFEvidence.MyAFEvidence.NON_AF;

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
public class BleEcgRecord10 extends BasicRecord implements ISignalRecord, IDiagnosable, Serializable {
    private int sampleRate = 0; // sample rate
    private int caliValue = 0; // calibration value of 1mV
    private int leadTypeCode = 0; // lead type code
    private final List<Short> ecgData = new ArrayList<>(); // ecg data
    private final EcgReport report = new EcgReport();
    @Column(ignore = true)
    private int pos = 0; // current position to the ecgData

    private BleEcgRecord10(long createTime, String devAddress, Account creator) {
        super(ECG, createTime, devAddress, creator);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        sampleRate = json.getInt("sampleRate");
        caliValue = json.getInt("caliValue");
        leadTypeCode = json.getInt("leadTypeCode");

        ecgData.clear();
        String ecgDataStr = json.getString("ecgData");
        String[] strings = ecgDataStr.split(",");
        for(String str : strings) {
            ecgData.add(Short.parseShort(str));
        }

        if(json.has("report")) {
            report.fromJson(json.getJSONObject("report"));
        }
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("caliValue", caliValue);
        json.put("leadTypeCode", leadTypeCode);
        json.put("ecgData", RecordUtil.listToString(ecgData));
        json.put("report", report.toJson());
        return json;
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

    public boolean process(short ecg) {
        ecgData.add(ecg);
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + leadTypeCode + "-" + ecgData + "-" + report;
    }

    @Override
    public void retrieveDiagnoseResult(Context context, IWebCallback callback) {
        processReport(context, REPORT_CMD_DOWNLOAD, callback);
    }

    @Override
    public void requestDiagnose() {
        EcgProcessor ecgProc = new EcgProcessor();
        ecgProc.process(ecgData, sampleRate);
        int aveHr = ecgProc.getAverageHr();

        List<Double> RR = ecgProc.getRRIntervalInMs();
        MyAFEvidence afEvi = MyAFEvidence.getInstance();
        afEvi.process(RR);
        int afe = afEvi.getAFEvidence();
        int classify = afEvi.getClassifyResult();

        StringBuilder builder = new StringBuilder();
        builder.append("房颤变异值：").append(afe).append("\n");
        if(classify == MyAFEvidence.AF) {
            builder.append("*您的心跳节律不规则，具有房颤风险。如有心脏不适症状，请及时就医。");
        } else if(classify == NON_AF){
            builder.append("未发现房颤异常。");
        } else {
            builder.append("由于信号质量较差，无法判断是否有房颤。");
        }

        report.setReportTime(new Date().getTime());
        report.setContent(builder.toString());
        report.setStatus(EcgReport.DONE);
        report.setAveHr(aveHr);
        report.save();
        setNeedUpload(true);
        save();
    }

    private void processReport(Context context, int cmd, IWebCallback callback) {
        if(needUpload()) {
            upload(context, new IWebCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    if (code == RETURN_CODE_SUCCESS) {
                        doProcessRequest(context, cmd, callback);
                    } else {
                        Toast.makeText(context, R.string.operation_failure, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            doProcessRequest(context, cmd, callback);
        }
    }

    private void doProcessRequest(Context context, int cmd, IWebCallback callback) {
        new ReportWebAsyncTask(context, cmd, callback).execute(this);
    }

    private static class ReportWebAsyncTask extends AsyncTask<BleEcgRecord10, Void, Object[]> {
        private static final int WAIT_TASK_SECOND = 10;

        private IWebCallback callback;
        private ProgressDialog progressDialog;
        private int cmd;

        public ReportWebAsyncTask(Context context, int cmd, IWebCallback callback) {
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
            final Object[] result = {RETURN_CODE_WEB_FAILURE, null};
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

            switch (cmd) {
                case REPORT_CMD_REQUEST:
                    KMWebServiceUtil.requestReport(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, callback);
                    break;
                case REPORT_CMD_DOWNLOAD:
                    KMWebServiceUtil.downloadReport(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record, callback);
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
            progressDialog.dismiss();
        }
    }
}
