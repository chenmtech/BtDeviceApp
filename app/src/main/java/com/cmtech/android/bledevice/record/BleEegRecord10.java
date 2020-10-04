package com.cmtech.android.bledevice.record;

import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.model.Account;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.record.RecordType.EEG;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.record
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
    private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private int recordSecond; // unit: s
    private List<Integer> eegData; // eeg data
    @Column(ignore = true)
    private int pos = 0;

    private BleEegRecord10() {
        super(EEG);
        initData();
        recordSecond = 0;
    }

    private BleEegRecord10(long createTime, String devAddress, Account creator, String note) {
        super(EEG, "1.0", createTime, devAddress, creator, note, true);
        initData();
        recordSecond = 0;
    }

    private BleEegRecord10(JSONObject json) throws JSONException{
        super(json, false);
        initData();
        recordSecond = json.getInt("recordSecond");
    }

    private void initData() {
        sampleRate = 0;
        caliValue = 0;
        leadTypeCode = 0;
        eegData = new ArrayList<>();
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = super.toJson();
            json.put("sampleRate", sampleRate);
            json.put("caliValue", caliValue);
            json.put("leadTypeCode", leadTypeCode);
            json.put("recordSecond", recordSecond);
            StringBuilder builder = new StringBuilder();
            for(Integer ele : eegData) {
                builder.append(ele).append(',');
            }
            json.put("eegData", builder.toString());
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void fromJson(JSONObject json) {
        try{
            sampleRate = json.getInt("sampleRate");
            caliValue = json.getInt("caliValue");
            leadTypeCode = json.getInt("leadTypeCode");
            recordSecond = json.getInt("recordSecond");
            String eegDataStr = json.getString("eegData");
            List<Integer> eegData = new ArrayList<>();
            String[] strings = eegDataStr.split(",");
            for(String str : strings) {
                eegData.add(Integer.parseInt(str));
            }
            this.eegData = eegData;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
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

    @Override
    public int getRecordSecond() {
        return recordSecond;
    }

    public void setRecordSecond(int recordSecond) {
        this.recordSecond = recordSecond;
    }

    public boolean process(int eeg) {
        eegData.add(eeg);
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + leadTypeCode + "-" + recordSecond + "-" + eegData;
    }
}
