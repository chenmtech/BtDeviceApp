package com.cmtech.android.bledevice.record;

import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.model.Account;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cmtech.android.bledevice.record.RecordType.THERMO;
import static com.cmtech.android.bledevice.report.EcgReport.DEFAULT_VER;

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
    private float highestTemp = 0.0f;
    private List<Float> temp = new ArrayList<>();

    private BleThermoRecord10() {
        super(THERMO);
    }

    private BleThermoRecord10(long createTime, String devAddress, Account creator) {
        super(THERMO, createTime, devAddress, creator);
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = super.toJson();
            StringBuilder builder = new StringBuilder();
            for(Float ele : temp) {
                builder.append(ele).append(',');
            }
            json.put("temp", builder.toString());
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void fromJson(JSONObject json) {
        try {
            String tempStr = json.getString("temp");
            List<Float> temp = new ArrayList<>();
            String[] strings = tempStr.split(",");
            for(String str : strings) {
                temp.add(Float.parseFloat(str));
            }
            this.temp = temp;
            this.highestTemp = Collections.max(temp);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
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
