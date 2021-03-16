package com.cmtech.android.bledeviceapp.data.record;

import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.util.ListStringUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.PPG;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      BlePpgRecord10
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2021/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BlePpgRecord extends BasicRecord implements ISignalRecord, Serializable {
    private int sampleRate = 0; // sample rate
    private int caliValue = 0; // calibration value
    private final List<Integer> ppgData = new ArrayList<>(); // ppg data

    @Column(ignore = true)
    private int pos = 0;

    private BlePpgRecord(String ver, long createTime, String devAddress, int creatorId) {
        super(PPG, ver, createTime, devAddress, creatorId);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        sampleRate = json.getInt("sampleRate");
        caliValue = json.getInt("caliValue");
        ListStringUtil.stringToList(json.getString("eegData"), ppgData, Integer.class);
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("caliValue", caliValue);
        json.put("eegData", ListStringUtil.listToString(ppgData));
        return json;
    }

    @Override
    public boolean noSignal() {
        return ppgData.isEmpty();
    }

    public List<Integer> getPpgData() {
        return ppgData;
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

    @Override
    public boolean isEOD() {
        return (pos >= ppgData.size());
    }

    @Override
    public void seekData(int pos) {
        this.pos = pos;
    }

    @Override
    public int readData() throws IOException {
        if(pos >= ppgData.size()) throw new IOException();
        return ppgData.get(pos++);
    }

    @Override
    public int getDataNum() {
        return ppgData.size();
    }

    public boolean process(int ppg) {
        ppgData.add(ppg);
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + ppgData;
    }
}
