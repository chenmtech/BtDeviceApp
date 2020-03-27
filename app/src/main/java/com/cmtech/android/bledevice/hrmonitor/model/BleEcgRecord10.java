package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.bmefile.DataIOUtil;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.hrmonitor.model.HRMonitorDevice.INVALID_HEART_RATE;
import static com.cmtech.android.bledeviceapp.AppConstant.DIR_CACHE;
import static com.cmtech.android.bledeviceapp.model.Account.PLAT_NAME_CHAR_LEN;
import static com.cmtech.android.bledeviceapp.model.Account.USER_ID_CHAR_LEN;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      EcgRecord10
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午7:11
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleEcgRecord10 extends LitePalSupport {
    private static final byte[] ECG = {'E', 'C', 'G'}; // indication of ecg record
    private static final int DEVICE_ADDRESS_CHAR_NUM = 12; // char num of device address

    private int id;
    private byte[] ver = new byte[2]; // ecg record version
    private long createTime; //
    private String devAddress; //
    private String creatorPlat;
    private String creatorId;
    private int sampleRate; // 采样频率
    private int caliValue; // 标定值
    private int leadTypeCode; // 导联类型代码
    private List<Short> ecgList; // list of the filtered HR

    private BleEcgRecord10() {
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
        sampleRate = 0;
        caliValue = 0;
        leadTypeCode = 0;
        ecgList = new ArrayList<>();
    }

    public String getRecordName() {
        return createTime + devAddress;
    }
    public int getId() {
        return id;
    }
    public long getCreateTime() {
        return createTime;
    }
    public String getDevAddress() {
        return devAddress;
    }
    public String getCreatorPlat() {
        return creatorPlat;
    }
    public String getCreatorName() {
        Account account = LitePal.where("platName = ? and userId = ?", creatorPlat, creatorId).findFirst(Account.class);
        if(account == null)
            return creatorId;
        else {
            return account.getName();
        }
    }
    public List<Short> getEcgList() {
        return ecgList;
    }

    public boolean process(short ecg) {
        ecgList.add(ecg);
        return true;
    }

    // create new ecg record
    public static BleEcgRecord10 create(byte[] ver, String devAddress, Account creator, int sampleRate, int caliValue, int leadTypeCode) {
        if(creator == null) {
            throw new NullPointerException("The creator is null.");
        }
        if(DIR_CACHE == null) {
            throw new NullPointerException("The cache dir is null");
        }
        if(ver == null || ver.length != 2 || ver[0] != 0x01 || ver[1] != 0x00) return null;

        BleEcgRecord10 record = new BleEcgRecord10();
        record.ver[0] = 0x01;
        record.ver[1] = 0x00;
        record.createTime = new Date().getTime();
        record.devAddress = devAddress;
        record.creatorPlat = creator.getPlatName();
        record.creatorId = creator.getUserId();
        record.sampleRate = sampleRate;
        record.caliValue = caliValue;
        record.leadTypeCode = leadTypeCode;
        return record;
    }

    @Override
    public String toString() {
        return createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId + "-" + ecgList;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        BleEcgRecord10 other = (BleEcgRecord10) otherObject;
        return getRecordName().equals(other.getRecordName());
    }

    @Override
    public int hashCode() {
        return getRecordName().hashCode();
    }

}
