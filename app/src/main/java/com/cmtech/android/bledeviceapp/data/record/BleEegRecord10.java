package com.cmtech.android.bledeviceapp.data.record;

import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.RecordUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.EEG;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      BleEegRecord10
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午7:11
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleEegRecord10 extends BasicRecord implements ISignalRecord, Serializable {
    private int sampleRate = 0; // sample rate
    private int caliValue = 0; // calibration value of 1mV
    private int leadTypeCode = 0; // lead type code
    private final List<Integer> eegData = new ArrayList<>(); // eeg data

    @Column(ignore = true)
    private int pos = 0;

    private BleEegRecord10(long createTime, String devAddress, Account creator) {
        super(EEG, createTime, devAddress, creator);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        sampleRate = json.getInt("sampleRate");
        caliValue = json.getInt("caliValue");
        leadTypeCode = json.getInt("leadTypeCode");
        RecordUtil.stringToList(json.getString("eegData"), eegData, Integer.class);
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("caliValue", caliValue);
        json.put("leadTypeCode", leadTypeCode);
        json.put("eegData", RecordUtil.listToString(eegData));
        return json;
    }

    @Override
    public boolean noSignal() {
        return eegData.isEmpty();
    }

    public List<Integer> getEegData() {
        return eegData;
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

    @Override
    public boolean isEOD() {
        return (pos >= eegData.size());
    }

    @Override
    public void seekData(int pos) {
        this.pos = pos;
    }

    @Override
    public int readData() throws IOException {
        if(pos >= eegData.size()) throw new IOException();
        return eegData.get(pos++);
    }

    @Override
    public int getDataNum() {
        return eegData.size();
    }

    public boolean process(int eeg) {
        eegData.add(eeg);
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + leadTypeCode + "-" + eegData;
    }
}
