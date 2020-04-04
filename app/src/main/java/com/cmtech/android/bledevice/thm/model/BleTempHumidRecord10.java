package com.cmtech.android.bledevice.thm.model;

import com.cmtech.android.bledevice.interf.AbstractRecord;
import com.cmtech.android.bledeviceapp.model.Account;

import java.util.Date;

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

    private BleTempHumidRecord10() {
        super();
        temperature = 0.0f;
        humid = 0.0f;
        heatIndex = 0.0f;
        location = "室内";
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

    public static BleTempHumidRecord10 create(byte[] ver, String devAddress, Account creator) {
        if(creator == null) {
            throw new NullPointerException("The creator is null.");
        }
        if(DIR_CACHE == null) {
            throw new NullPointerException("The cache dir is null");
        }
        if(ver == null || ver.length != 2 || ver[0] != 0x01 || ver[1] != 0x00) return null;

        BleTempHumidRecord10 record = new BleTempHumidRecord10();
        record.setVer(ver);
        record.setCreateTime(new Date().getTime());
        record.setDevAddress(devAddress);
        record.setCreator(creator);
        record.temperature = 0.0f;
        record.humid = 0.0f;
        record.heatIndex = 0.0f;
        record.location = "室内";
        return record;
    }
}
