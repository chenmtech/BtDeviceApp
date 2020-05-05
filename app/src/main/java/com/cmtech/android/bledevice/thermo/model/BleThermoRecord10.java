package com.cmtech.android.bledevice.thermo.model;

import com.cmtech.android.bledevice.common.AbstractRecord;
import com.cmtech.android.bledevice.hrm.model.BleHrRecord10;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.common.RecordType.THERMO;
import static com.cmtech.android.bledeviceapp.AppConstant.DIR_CACHE;

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

    public BleThermoRecord10(long createTime, String devAddress, Account creator) {
        super(THERMO, "1.0", createTime, devAddress, creator);
        highestTemp = 0.0f;
        temp = new ArrayList<>();
    }

    @Override
    public boolean isDataEmpty() {
        return true;
    }

    @Override
    public boolean setDataFromJson(JSONObject json) {
        return false;
    }

    @Override
    public String getDesc() {
        return "体温"+getHighestTemp();
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

    public static List<BleThermoRecord10> createFromLocalDb(Account creator, long fromTime, int num) {
        return LitePal.select("createTime, devAddress, creatorPlat, creatorId, highestTemp")
                .where("creatorPlat = ? and creatorId = ? and createTime < ?", creator.getPlatName(), creator.getPlatId(), ""+fromTime)
                .order("createTime desc").limit(num).find(BleThermoRecord10.class);
    }

    @Override
    public String toString() {
        return super.toString() + "-" + highestTemp + "-" + temp;
    }
}
