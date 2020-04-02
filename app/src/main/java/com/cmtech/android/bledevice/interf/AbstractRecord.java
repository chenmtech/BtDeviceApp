package com.cmtech.android.bledevice.interf;

import com.cmtech.android.bledeviceapp.model.Account;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.interf
 * ClassName:      AbstractRecord
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/2 下午2:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/2 下午2:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public abstract class AbstractRecord extends LitePalSupport implements IRecord {
    private int id;
    private byte[] ver = new byte[2]; // hr record version
    private long createTime; //
    private String devAddress; //
    private String creatorPlat;
    private String creatorId;

    protected AbstractRecord() {
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
    }

    public int getId() {
        return id;
    }
    public void setVer(byte[] ver) {
        this.ver[0] = ver[0];
        this.ver[1] = ver[1];
    }
    public long getCreateTime() {
        return createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public String getDevAddress() {
        return devAddress;
    }
    public void setDevAddress(String devAddress) {
        this.devAddress = devAddress;
    }
    public String getRecordName() {
        return createTime + devAddress;
    }
    public String getCreatorPlat() {
        return creatorPlat;
    }
    public void setCreator(Account creator) {
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
    }
    public String getCreatorName() {
        Account account = LitePal.where("platName = ? and platId = ?", creatorPlat, creatorId).findFirst(Account.class);
        if(account == null)
            return creatorId;
        else {
            return account.getName();
        }
    }

    @Override
    public String toString() {
        return createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        IRecord other = (IRecord) otherObject;
        return getRecordName().equals(other.getRecordName());
    }

    @Override
    public int hashCode() {
        return getRecordName().hashCode();
    }
}
