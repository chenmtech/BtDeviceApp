package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.THERMO;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.util.ListStringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.thermo.model
 * ClassName:      ThermoRecord10
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/3 下午3:58
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/3 下午3:58
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleThermoRecord extends BasicRecord {
    private final List<Float> temp = new ArrayList<>();
    private float highestTemp = 0.0f;

    private BleThermoRecord(String ver, int accountId, long createTime, String devAddress,
                            int sampleRate, int channelNum, String gain, String unit) {
        super(THERMO, ver, accountId, createTime, devAddress, sampleRate, channelNum, 4, gain, unit);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        ListStringUtil.stringToList(json.getString("temp"), temp, Float.class);
        this.highestTemp = Collections.max(temp);
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();

        json.put("temp", ListStringUtil.listToString(temp));

        return json;
    }

    @Override
    public boolean noSignalFile() {
        return temp.isEmpty();
    }

    public List<Float> getTemp() {
        return temp;
    }

    public float getHighestTemp() {
        return highestTemp;
    }

    public void setHighestTemp(float highestTemp) {
        this.highestTemp = highestTemp;
    }

    public void addTemp(float temp) {
        this.temp.add(temp);
    }

    @Override
    public int[] readData() throws IOException {
        throw new IOException("Error!");
    }

    @Override
    public int getDataNum() {
        throw new IllegalStateException("");
    }

    @Override
    public List<Integer> getGain() {
        throw new IllegalStateException("");
    }

    @Override
    public int getSampleRate() {
        throw new IllegalStateException("");
    }

    @Override
    public boolean isEOD() {
        throw new IllegalStateException("");
    }

    @Override
    public void seek(int pos) {
        throw new IllegalStateException("");
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + highestTemp + "-" + temp;
    }
}
