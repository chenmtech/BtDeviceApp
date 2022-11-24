package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.TH;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.thm.model
 * ClassName:      BleTempHumidRecord10
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/4 下午3:11
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/4 下午3:11
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleTempHumidRecord extends BasicRecord {
    private float temperature = 0.0f;
    private float humid = 0.0f;
    private float heatIndex = 0.0f;
    private String location = "室内";

    private BleTempHumidRecord(String ver, int accountId, long createTime, String devAddress,
                               int sampleRate, int channelNum, String gain, String unit) {
        super(TH, ver, accountId, createTime, devAddress, sampleRate, channelNum, 4, gain, unit);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        return null;
    }

    @Override
    public boolean noSignalFile() {
        return true;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getHumid() {
        return humid;
    }

    public void setHumid(float humid) {
        this.humid = humid;
    }

    public float getHeatIndex() {
        return heatIndex;
    }

    public void setHeatIndex(float heatIndex) {
        this.heatIndex = heatIndex;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

}
