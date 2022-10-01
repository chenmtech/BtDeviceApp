package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.PPG;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.util.ListStringUtil;

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
    //-----------------------------------------常量
    // 记录每个数据的字节数
    private static final int BYTES_PER_DATUM = 4;

    private int sampleRate = 0; // sample rate
    private int caliValue = 0; // calibration value
    //private final List<Integer> ppgData = new ArrayList<>(); // ppg data

    private BlePpgRecord(String ver, long createTime, String devAddress, int creatorId) {
        super(PPG, ver, createTime, devAddress, creatorId);
    }

    // 创建信号文件
    public void createSigFile() {
        super.createSigFile(BYTES_PER_DATUM);
    }

    // 打开信号文件
    public void openSigFile() {
        super.openSigFile(BYTES_PER_DATUM);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        sampleRate = json.getInt("sampleRate");
        caliValue = json.getInt("caliValue");
        //ListStringUtil.stringToList(json.getString("ppgData"), ppgData, Integer.class);
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("caliValue", caliValue);
        //json.put("ppgData", ListStringUtil.listToString(ppgData));
        return json;
    }

    /*
    public List<Integer> getPpgData() {
        return ppgData;
    }

    public void setPpgData(List<Integer> ppgData) {
        this.ppgData.addAll(ppgData);
    }
    */

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
    public int readData() throws IOException {
        if(sigFile == null) throw new IOException();
        return sigFile.readInt();
    }

    public boolean process(int ppg) {
        boolean success = false;
        try {
            if(sigFile != null) {
                sigFile.writeInt(ppg);
                success = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue;
    }
}
