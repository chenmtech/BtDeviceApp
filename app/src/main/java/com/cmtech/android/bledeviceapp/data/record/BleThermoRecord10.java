package com.cmtech.android.bledeviceapp.data.record;

import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.ListStringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.THERMO;

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
public class BleThermoRecord10 extends BasicRecord {
    private final List<Float> temp = new ArrayList<>();
    private float highestTemp = 0.0f;

    private BleThermoRecord10(long createTime, String devAddress, Account creator) {
        super(THERMO, createTime, devAddress, creator);
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
    public boolean noSignal() {
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

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + highestTemp + "-" + temp;
    }
}
