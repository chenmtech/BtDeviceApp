package com.cmtech.android.bledeviceapp.data.record;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.asynctask.ReportAsyncTask;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.IEcgArrhythmiaDetector;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.MyEcgArrhythmiaDetector;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.util.ListStringUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.ECG;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      BleEcgRecord10
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午7:11
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleEcgRecord extends BasicRecord implements ISignalRecord, IDiagnosable, Serializable {
    private int sampleRate = 0; // sample rate
    private int caliValue = 0; // calibration value of 1mV
    private int leadTypeCode = 0; // lead type code
    private final List<Short> ecgData = new ArrayList<>(); // ecg data
    private final EcgReport report = new EcgReport();
    @Column(ignore = true)
    private int pos = 0; // current position to the ecgData

    private BleEcgRecord(String ver, long createTime, String devAddress, int creatorId) {
        super(ECG, ver, createTime, devAddress, creatorId);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        sampleRate = json.getInt("sampleRate");
        caliValue = json.getInt("caliValue");
        leadTypeCode = json.getInt("leadTypeCode");
        ListStringUtil.stringToList(json.getString("ecgData"), ecgData, Short.class);
        if(json.has("report")) {
            report.fromJson(json.getJSONObject("report"));
        }
        report.save();
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("caliValue", caliValue);
        json.put("leadTypeCode", leadTypeCode);
        json.put("ecgData", ListStringUtil.listToString(ecgData));
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
    public void retrieveDiagnoseResult(Context context, IWebResponseCallback callback) {
        processReport(context, CMD_DOWNLOAD_REPORT, callback);
    }

    @Override
    public void requestDiagnose() {
        IEcgArrhythmiaDetector algorithm = new MyEcgArrhythmiaDetector();
        EcgReport rtnReport = algorithm.process(this);
        report.setVer(rtnReport.getVer());
        report.setReportTime(rtnReport.getReportTime());
        report.setContent(rtnReport.getContent());
        report.setStatus(rtnReport.getStatus());
        report.setAveHr(rtnReport.getAveHr());
        report.save();
        setNeedUpload(true);
        save();
    }

    private void processReport(Context context, int cmd, IWebResponseCallback callback) {
        if(needUpload()) {
            upload(context, new ICodeCallback() {
                @Override
                public void onFinish(int code) {
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

    private void doProcessRequest(Context context, int cmd, IWebResponseCallback callback) {
        new ReportAsyncTask(context, cmd, callback).execute(this);
    }

}
