package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.PTT;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.util.ListStringUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      BlePttRecord
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2021/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BlePttRecord extends BasicRecord implements ISignalRecord, Serializable {
    private int sampleRate = 0; // sample rate
    private int ecgCaliValue = 0; // ecg calibration value
    private final List<Short> ecgData = new ArrayList<>(); // ecg data
    private int ppgCaliValue = 0; // ppg calibration value
    private final List<Integer> ppgData = new ArrayList<>(); // ppg data

    @Column(ignore = true)
    private int pos = 0;

    private BlePttRecord(String ver, long createTime, String devAddress, int creatorId) {
        super(PTT, ver, createTime, devAddress, creatorId);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        sampleRate = json.getInt("sampleRate");
        ecgCaliValue = json.getInt("ecgCaliValue");
        //ListStringUtil.stringToList(json.getString("ecgData"), ecgData, Short.class);
        ppgCaliValue = json.getInt("ppgCaliValue");
        //ListStringUtil.stringToList(json.getString("ppgData"), ppgData, Integer.class);
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("ecgCaliValue", ecgCaliValue);
        json.put("ecgData", ListStringUtil.listToString(ecgData));
        json.put("ppgCaliValue", ppgCaliValue);
        json.put("ppgData", ListStringUtil.listToString(ppgData));
        return json;
    }

    @Override
    public boolean noSignal() {
        return ecgData.isEmpty() || ppgData.isEmpty();
    }

    public List<Short> getEcgData() {
        return ecgData;
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
        throw new IllegalArgumentException();
    }

    public void setCaliValue(int caliValue) {
        throw new IllegalArgumentException();
    }

    public int getEcgCaliValue() {
        return ecgCaliValue;
    }

    public void setEcgCaliValue(int ecgCaliValue) {
        this.ecgCaliValue = ecgCaliValue;
    }

    public int getPpgCaliValue() {
        return ppgCaliValue;
    }

    public void setPpgCaliValue(int ppgCaliValue) {
        this.ppgCaliValue = ppgCaliValue;
    }

    @Override
    public boolean isEOD() {
        return (pos >= ppgData.size() || pos >= ecgData.size());
    }

    @Override
    public void seekData(int pos) {
        this.pos = pos;
    }

    @Override
    public int readData() throws IOException {
        throw new IOException();
    }

    public int readEcgData() throws IOException {
        if(pos >= ecgData.size()) throw new IOException();
        return ecgData.get(pos++);
    }

    public int readPpgData() throws IOException {
        if(pos >= ppgData.size()) throw new IOException();
        return ppgData.get(pos++);
    }

    @Override
    public int getDataNum() {
        return ppgData.size();
    }

    public boolean process(int ecg, int ppg) {
        ecgData.add((short)ecg);
        ppgData.add(ppg);
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + ecgCaliValue + "-" + ecgData + "-" + ppgCaliValue + "-" + ppgData;
    }
}
