package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.AccountManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.record.RecordType.ECG;

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
public class BleEcgRecord10 extends CommonRecord implements IEcgRecord, Serializable {
    private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private int recordSecond; // unit: s
    private List<Short> ecgData; // ecg data
    @Column(ignore = true)
    private int pos = 0;

    BleEcgRecord10(long createTime, String devAddress, User creator, String note) {
        super(ECG, "1.0", createTime, devAddress, creator, note);
        sampleRate = 0;
        caliValue = 0;
        leadTypeCode = 0;
        recordSecond = 0;
        ecgData = new ArrayList<>();
    }

    BleEcgRecord10(JSONObject json) {
        super(ECG, "1.0", json);
        sampleRate = 0;
        caliValue = 0;
        leadTypeCode = 0;
        recordSecond = 0;
        ecgData = new ArrayList<>();
    }

    static BleEcgRecord10 createFromJson(JSONObject json) {
        if(json == null) {
            throw new NullPointerException("The json is null.");
        }

        try {
            String devAddress = json.getString("devAddress");
            long createTime = json.getLong("createTime");
            User account = AccountManager.getAccount();
            int recordSecond = json.getInt("recordSecond");
            String note = json.getString("note");

            BleEcgRecord10 newRecord = new BleEcgRecord10(createTime, devAddress, account, note);
            newRecord.setRecordSecond(recordSecond);
            return newRecord;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    static List<BleEcgRecord10> createFromLocalDb(User creator, long fromTime, int num) {
        return LitePal.select("createTime, devAddress, creatorPlat, creatorId, recordSecond, note, uploaded")
                .where("creatorPlat = ? and creatorId = ? and createTime < ?", creator.getPlatName(), creator.getPlatId(), ""+fromTime)
                .order("createTime desc").limit(num).find(BleEcgRecord10.class);
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = super.toJson();
        if(json == null) return null;
        try {
            json.put("recordTypeCode", getTypeCode());
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean setDataFromJson(JSONObject json) {
        if(json == null) {
            throw new NullPointerException("The json is null.");
        }

        try {
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean noData() {
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

    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + leadTypeCode + "-" + recordSecond + "-" + ecgData;
    }
}
