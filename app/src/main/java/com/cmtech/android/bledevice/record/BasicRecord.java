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
 * ClassName:      BasicRecord
 * Description:    各种记录类的共同基本信息基类
 * Author:         chenm
 * CreateDate:     2020/4/2 下午2:44
 * UpdateUser:     chenm
 * UpdateDate:     2020/6/10 下午2:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BasicRecord extends LitePalSupport implements IRecord {
    private int id;
    private String ver; // record version
    private long createTime; //
    private String devAddress; //
    private String creatorPlat;
    private String creatorId;
    private String note;
    private boolean modified;
    @Column(ignore = true)
    private RecordType type;

    private BasicRecord(RecordType type) {
        ver = "";
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
        note = "";
        modified = true;
        this.type = type;
    }

    BasicRecord(RecordType type, String ver, long createTime, String devAddress, User creator, String note) {
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
        modified = true;
    }

    BasicRecord(RecordType type, String ver, JSONObject json) {
        if(json == null) {
            throw new NullPointerException("The json is null.");
        }

        this.type = type;
        this.ver = ver;

        try {
            createTime = json.getLong("createTime");
            devAddress = json.getString("devAddress");
            creatorPlat = AccountManager.getAccount().getPlatName();
            creatorId = AccountManager.getAccount().getPlatId();
            note = json.getString("note");
            modified = false;
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
    public boolean modified() {
        return modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
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
    public boolean parseDataFromJson(JSONObject json) {
        return false;
    }

    @Override
    public boolean lackData() {
        return true;
    }

    @Override
    public String toString() {
        return ver + "-" + createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId + "-" + note + "-" + modified;
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
