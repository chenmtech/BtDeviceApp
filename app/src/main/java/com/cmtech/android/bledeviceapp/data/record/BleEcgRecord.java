package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.ECG;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_HR;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.asynctask.ReportAsyncTask;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.IEcgArrhythmiaDetector;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.MyEcgArrhythmiaDetector;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.util.ListStringUtil;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      BleEcgRecord
 * Description:    心电信号记录类
 * Author:         chenm
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
    private int aveHr = INVALID_HR;
    private final List<Integer> breakPos = new ArrayList<>();
    private final List<Long> breakTime = new ArrayList<>();
    @Column(ignore = true)
    private int pos = 0; // current position of the ecgData in this record
    @Column(ignore = true)
    private boolean interrupt = false;


    private BleEcgRecord(String ver, long createTime, String devAddress, int creatorId) {
        super(ECG, ver, createTime, devAddress, creatorId);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        sampleRate = json.getInt("sampleRate");
        caliValue = json.getInt("caliValue");
        leadTypeCode = json.getInt("leadTypeCode");
        aveHr = json.getInt("aveHr");
        ListStringUtil.stringToList(json.getString("ecgData"), ecgData, Short.class);
        if(json.has("breakPos"))
            ListStringUtil.stringToList(json.getString("breakPos"), breakPos, Integer.class);
        if(json.has("breakTime"))
            ListStringUtil.stringToList(json.getString("breakTime"), breakTime, Long.class);
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("caliValue", caliValue);
        json.put("leadTypeCode", leadTypeCode);
        json.put("aveHr", aveHr);
        json.put("ecgData", ListStringUtil.listToString(ecgData));
        json.put("breakPos", ListStringUtil.listToString(breakPos));
        json.put("breakTime", ListStringUtil.listToString(breakTime));
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

    public int getAveHr() {
        return aveHr;
    }

    public void setAveHr(int aveHr) {
        this.aveHr = aveHr;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        ViseLog.e("hi " + interrupt);
        this.interrupt = interrupt;
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

    // 获取当前数据位置对应的时间
    public long getPosDatumTime() {
        int i;
        for(i = 0; i < breakPos.size(); i++) {
            if(breakPos.get(i) > pos) break;
        }
        i--;
        if(i < 0)
            return getCreateTime()+pos* 1000L /sampleRate;
        else
            return breakTime.get(i) + (pos - breakPos.get(i))*1000L/sampleRate;
    }

    /**
     * 处理一个ECG信号值
     * @param ecg
     * @return
     */
    public boolean process(short ecg) {
        ecgData.add(ecg);
        if(interrupt) {
            breakPos.add(ecgData.size());
            breakTime.add(new Date().getTime());
            interrupt = false;
            ViseLog.e(breakPos + "-" + breakTime);
        }
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + leadTypeCode + "-" + ecgData +
                "-" + breakPos + "-" + breakTime;
    }

    @Override
    public void remoteDiagnose(Context context, IWebResponseCallback callback) {
        new ReportAsyncTask(context, callback).execute(this);
    }

    @Override
    public void localDiagnose() {
        IEcgArrhythmiaDetector algorithm = new MyEcgArrhythmiaDetector();
        EcgReport rtnReport = algorithm.process(this);
        setReportVer(rtnReport.getVer());
        setReportProvider(rtnReport.getReportProvider());
        setReportTime(rtnReport.getReportTime());
        setReportContent(rtnReport.getReportContent());
        setReportStatus(rtnReport.getReportStatus());
        setAveHr(rtnReport.getAveHr());
        //setNeedUpload(true);
        save();
    }
}
