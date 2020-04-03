package com.cmtech.android.bledevice.thermo.model;

import com.cmtech.android.bledevice.hrm.model.BleHrRecord10;
import com.cmtech.android.bledevice.interf.AbstractRecord;
import com.cmtech.android.bledeviceapp.model.Account;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private BleThermoRecord10() {
        super();
        highestTemp = 0.0f;
        temp = new ArrayList<>();
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

    public static BleThermoRecord10 create(byte[] ver, String devAddress, Account creator) {
        if(creator == null) {
            throw new NullPointerException("The creator is null.");
        }
        if(DIR_CACHE == null) {
            throw new NullPointerException("The cache dir is null");
        }
        if(ver == null || ver.length != 2 || ver[0] != 0x01 || ver[1] != 0x00) return null;

        BleThermoRecord10 record = new BleThermoRecord10();
        record.setVer(ver);
        record.setCreateTime(new Date().getTime());
        record.setDevAddress(devAddress);
        record.setCreator(creator);
        record.highestTemp = 0.0f;
        record.temp = new ArrayList<>();
        return record;
    }

    @Override
    public String toString() {
        return super.toString() + "-" + highestTemp + "-" + temp;
    }
}
