package com.cmtech.android.bledevice.hrm.model;

import com.cmtech.android.bledevice.interf.AbstractRecord;
import com.cmtech.android.bledevice.interf.IEcgRecord;
import com.cmtech.android.bledeviceapp.model.Account;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledeviceapp.AppConstant.DIR_CACHE;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      EcgRecord10
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午7:11
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleEcgRecord10 extends AbstractRecord implements IEcgRecord, Serializable {
    private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private int recordSecond; // unit: s
    private String note; // record description
    private List<Short> ecgList; // ecg data
    @Column(ignore = true)
    private int pos = 0;

    private BleEcgRecord10() {
        super();
        sampleRate = 0;
        caliValue = 0;
        leadTypeCode = 0;
        recordSecond = 0;
        note = "";
        ecgList = new ArrayList<>();
    }

    @Override
    public String getDesc() {
        return "时长约"+getRecordSecond()+"秒";
    }

    public List<Short> getEcgList() {
        return ecgList;
    }
    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    @Override
    public int getCaliValue() {
        return caliValue;
    }

    @Override
    public boolean isEOD() {
        return (pos >= ecgList.size());
    }

    @Override
    public void seekData(int pos) {
        this.pos = pos;
    }

    @Override
    public int readData() throws IOException {
        if(pos >= ecgList.size()) throw new IOException();
        return ecgList.get(pos++);
    }

    @Override
    public int getDataNum() {
        return ecgList.size();
    }

    @Override
    public int getRecordSecond() {
        return recordSecond;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean save() {
        recordSecond = ecgList.size()/sampleRate;
        return super.save();
    }

    public boolean process(short ecg) {
        ecgList.add(ecg);
        return true;
    }

    // create new ecg record
    public static BleEcgRecord10 create(byte[] ver, String devAddress, Account creator, int sampleRate, int caliValue, int leadTypeCode) {
        if(creator == null) {
            throw new NullPointerException("The creator is null.");
        }
        if(DIR_CACHE == null) {
            throw new NullPointerException("The cache dir is null");
        }
        if(ver == null || ver.length != 2 || ver[0] != 0x01 || ver[1] != 0x00) return null;

        BleEcgRecord10 record = new BleEcgRecord10();
        record.setVer(ver);
        record.setCreateTime(new Date().getTime());
        record.setDevAddress(devAddress);
        record.setCreator(creator);

        record.sampleRate = sampleRate;
        record.caliValue = caliValue;
        record.leadTypeCode = leadTypeCode;
        record.recordSecond = 0;
        record.note = "";
        return record;
    }

    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + leadTypeCode + "-" + recordSecond + "-" + note + "-" + ecgList;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sampleRate", sampleRate);
            jsonObject.put("caliValue", caliValue);
            jsonObject.put("leadTypeCode", leadTypeCode);
            jsonObject.put("recordSecond", recordSecond);
            jsonObject.put("note", note);
            StringBuilder builder = new StringBuilder();
            for(Short ele : ecgList) {
                builder.append(ele).append(',');
            }
            jsonObject.put("ecgList", builder.toString());
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
