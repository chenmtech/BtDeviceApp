package com.cmtech.android.bledevice.common;

import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;
import com.cmtech.android.bledeviceapp.model.Account;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.common
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
    private String ver; // record version
    private long createTime; //
    private String devAddress; //
    private String creatorPlat;
    private String creatorId;
    @Column(ignore = true)
    private RecordType type;

    public AbstractRecord(RecordType type) {
        ver = "";
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
        this.type = type;
    }

    public AbstractRecord(RecordType type, String ver, long createTime, String devAddress, Account creator) {
        if(creator == null) {
            throw new NullPointerException("The creator is null.");
        }
        this.type = type;
        this.ver = ver;
        this.createTime = createTime;
        this.devAddress = devAddress;
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
    }

    public int getId() {
        return id;
    }
    public String getVer() {
        return ver;
    }
    public void setVer(String ver) {
        this.ver = ver;
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
    public String getName() {
        return createTime + devAddress;
    }
    public String getCreatorPlat() {
        return creatorPlat;
    }
    public String getCreatorId() {
        return creatorId;
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
    public int getTypeCode() {
        return type.getCode();
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("ver", ver);
            json.put("createTime", createTime);
            json.put("devAddress", devAddress);
            json.put("creatorPlat", creatorPlat);
            json.put("creatorId", creatorId);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return ver + "-" + createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId + "-" + getDesc();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        IRecord other = (IRecord) otherObject;
        return getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
