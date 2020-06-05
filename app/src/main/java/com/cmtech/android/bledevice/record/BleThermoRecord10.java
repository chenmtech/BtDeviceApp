package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.model.User;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.record.RecordType.THERMO;

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
public class BleThermoRecord10 extends AbstractRecord {
    private float highestTemp;
    private List<Float> temp;

    BleThermoRecord10(long createTime, String devAddress, User creator, String note) {
        super(THERMO, "1.0", createTime, devAddress, creator, note);
        highestTemp = 0.0f;
        temp = new ArrayList<>();
    }

    static List<BleThermoRecord10> createFromLocalDb(User creator, long fromTime, int num) {
        return LitePal.select("createTime, devAddress, creatorPlat, creatorId, highestTemp, note")
                .where("creatorPlat = ? and creatorId = ? and createTime < ?", creator.getPlatName(), creator.getPlatId(), ""+fromTime)
                .order("createTime desc").limit(num).find(BleThermoRecord10.class);
    }

    static BleThermoRecord10 createFromJson(JSONObject json) {
        return null;
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
    public boolean isDataEmpty() {
        return true;
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
    public String toString() {
        return super.toString() + "-" + highestTemp + "-" + temp + "-" + getNote();
    }
}
