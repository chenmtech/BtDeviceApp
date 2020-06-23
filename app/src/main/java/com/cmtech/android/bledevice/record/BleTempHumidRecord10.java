package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import static com.cmtech.android.bledevice.record.RecordType.TH;

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
public class BleTempHumidRecord10 extends BasicRecord {
    private float temperature;
    private float humid;
    private float heatIndex;
    private String location;

    private BleTempHumidRecord10() {
        super(TH);
    }

    private BleTempHumidRecord10(long createTime, String devAddress, User creator, String note) {
        super(TH, "1.0", createTime, devAddress, creator, note, true);
        temperature = 0.0f;
        humid = 0.0f;
        heatIndex = 0.0f;
        location = "室内";
    }

    private BleTempHumidRecord10(JSONObject json)  throws JSONException{
         super(TH, "1.0", json, false);
    }

    @Override
    public JSONObject toJson() {
        return null;
    }

    @Override
    public boolean setDataFromJson(JSONObject json) {
        return false;
    }

    @Override
    public boolean noData() {
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

}
