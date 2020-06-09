package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;

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
public class CommonRecord extends LitePalSupport implements IRecord {
    private int id;
    private String ver; // record version
    private long createTime; //
    private String devAddress; //
    private String creatorPlat;
    private String creatorId;
    private String note;
    private boolean uploaded;
    @Column(ignore = true)
    private RecordType type;

    private CommonRecord(RecordType type) {
        ver = "";
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
        note = "";
        uploaded = false;
        this.type = type;
    }

    CommonRecord(RecordType type, String ver, long createTime, String devAddress, User creator, String note) {
        if(creator == null) {
            throw new NullPointerException("The creator is null.");
        }
        this.type = type;
        this.ver = ver;
        this.createTime = createTime;
        this.devAddress = devAddress;
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
        this.note = note;
        uploaded = false;
    }

    CommonRecord(RecordType type, String ver, JSONObject json) {
        if(json == null) {
            throw new NullPointerException("The json is null.");
        }

        this.type = type;
        this.ver = ver;

        try {
            devAddress = json.getString("devAddress");
            createTime = json.getLong("createTime");
            creatorPlat = AccountManager.getAccount().getPlatName();
            creatorId = AccountManager.getAccount().getPlatId();
            note = json.getString("note");
            uploaded = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getVer() {
        return ver;
    }

    @Override
    public int getTypeCode() {
        return type.getCode();
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String getDevAddress() {
        return devAddress;
    }

    @Override
    public String getName() {
        return createTime + devAddress;
    }

    @Override
    public String getCreatorPlat() {
        return creatorPlat;
    }

    @Override
    public String getCreatorId() {
        return creatorId;
    }

    public void setCreator(User creator) {
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
    }

    @Override
    public String getCreatorName() {
        User account = LitePal.where("platName = ? and platId = ?", creatorPlat, creatorId).findFirst(User.class);
        if(account == null)
            return creatorId;
        else {
            return account.getName();
        }
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean uploaded() {
        return uploaded;
    }

    @Override
    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
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
            json.put("note", note);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean setDataFromJson(JSONObject json) {
        return false;
    }

    @Override
    public boolean noData() {
        return true;
    }

    @Override
    public String toString() {
        return ver + "-" + createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId + "-" + note + "-" + uploaded;
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
