package com.cmtech.android.bledevice.thm.model;

import com.cmtech.android.bledevice.common.AbstractRecord;
import com.cmtech.android.bledeviceapp.model.Account;

import org.json.JSONObject;

import java.util.Date;

import static com.cmtech.android.bledevice.common.RecordType.TH;
import static com.cmtech.android.bledeviceapp.AppConstant.DIR_CACHE;

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
public class BleTempHumidRecord10 extends AbstractRecord {
    private float temperature;
    private float humid;
    private float heatIndex;
    private String location;

    public BleTempHumidRecord10(long createTime, String devAddress, Account creator) {
        super(TH, "1.0", createTime, devAddress, creator);
        temperature = 0.0f;
        humid = 0.0f;
        heatIndex = 0.0f;
        location = "室内";
    }

    @Override
    public boolean setDataFromJson(JSONObject json) {
        return false;
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
    public String getDesc() {
        return "地点" + location + "，温度" + temperature + "℃，湿度" + humid + "%，体感" + (int)heatIndex;
    }

    @Override
    public boolean isDataEmpty() {
        return true;
    }

}
