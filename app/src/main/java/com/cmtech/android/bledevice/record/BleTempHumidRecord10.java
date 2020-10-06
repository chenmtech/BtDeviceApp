package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.model.Account;

import org.json.JSONException;
import org.json.JSONObject;

import static com.cmtech.android.bledevice.record.RecordType.TH;
import static com.cmtech.android.bledevice.report.EcgReport.DEFAULT_VER;

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
    private float temperature = 0.0f;
    private float humid = 0.0f;
    private float heatIndex = 0.0f;
    private String location = "室内";

    private BleTempHumidRecord10() {
        super(TH);
    }

    private BleTempHumidRecord10(long createTime, String devAddress, Account creator) {
        super(TH, createTime, devAddress, creator);
    }

    @Override
    public JSONObject toJson() {
        return null;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        return;
    }

    @Override
    public boolean noSignal() {
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
