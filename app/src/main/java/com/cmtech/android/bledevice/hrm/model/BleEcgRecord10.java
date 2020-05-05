package com.cmtech.android.bledevice.hrm.model;

import com.cmtech.android.bledevice.common.AbstractRecord;
import com.cmtech.android.bledevice.common.IEcgRecord;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.common.RecordType.ECG;
import static com.cmtech.android.bledevice.hrm.model.RecordWebAsyncTask.DOWNLOAD_NUM_PER_TIME;

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
    private List<Short> ecgData; // ecg data
    @Column(ignore = true)
    private int pos = 0;

    public BleEcgRecord10(long createTime, String devAddress, Account creator) {
        super(ECG, "1.0", createTime, devAddress, creator);
        sampleRate = 0;
        caliValue = 0;
        leadTypeCode = 0;
        recordSecond = 0;
        note = "";
        ecgData = new ArrayList<>();
    }

    @Override
    public String getDesc() {
        return "时长约"+getRecordSecond()+"秒";
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isDataEmpty() {
        return ecgData.isEmpty();
    }

    public boolean process(short ecg) {
        ecgData.add(ecg);
        return true;
    }

    public static BleEcgRecord10 createFromJson(JSONObject json) {
        if(json == null) {
            throw new NullPointerException("The json is null.");
        }

        try {
            String devAddress = json.getString("devAddress");
            long createTime = json.getLong("createTime");
            Account account = AccountManager.getAccount();
            int recordSecond = json.getInt("recordSecond");

            BleEcgRecord10 newRecord = new BleEcgRecord10(createTime, devAddress, account);
            newRecord.setRecordSecond(recordSecond);
            return newRecord;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<BleEcgRecord10> createFromLocalDb(Account creator, long fromTime, int num) {
        return LitePal.select("createTime, devAddress, creatorPlat, creatorId, recordSecond")
                .where("creatorPlat = ? and creatorId = ? and createTime < ?", creator.getPlatName(), creator.getPlatId(), ""+fromTime)
                .order("createTime desc").limit(num).find(BleEcgRecord10.class);
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
            note = json.getString("note");
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
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + leadTypeCode + "-" + recordSecond + "-" + note + "-" + ecgData;
    }

    public JSONObject toJson() {
        JSONObject json = super.toJson();
        if(json == null) return null;
        try {
            json.put("recordTypeCode", getTypeCode());
            json.put("sampleRate", sampleRate);
            json.put("caliValue", caliValue);
            json.put("leadTypeCode", leadTypeCode);
            json.put("recordSecond", recordSecond);
            json.put("note", note);
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
}
