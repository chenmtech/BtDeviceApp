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
 * Package:        com.cmtech.android.bledevice.record
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
    public static final String QUERY_STR = "createTime, devAddress, creatorPlat, creatorId, note, needUpload"; // used to create from local DB

    private int id;
    private String ver; // record version
    private long createTime; // create time
    private String devAddress; // device address
    private String creatorPlat; // creator plat name
    private String creatorId; // creator plat ID
    private String note; // note
    private boolean needUpload; // need uploaded
    @Column(ignore = true)
    private RecordType type; // record type

    BasicRecord(RecordType type) {
        ver = "";
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
        note = "";
        needUpload = true;
        this.type = type;
    }

    BasicRecord(RecordType type, String ver, long createTime, String devAddress, User creator, String note, boolean needUpload) {
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
        this.needUpload = needUpload;
    }

    BasicRecord(RecordType type, String ver, JSONObject json, boolean needUpload) throws JSONException{
        if(json == null) {
            throw new NullPointerException("The json is null.");
        }
        this.type = type;
        this.ver = ver;
        this.createTime = json.getLong("createTime");
        this.devAddress = json.getString("devAddress");
        this.creatorPlat = AccountManager.getAccount().getPlatName();
        this.creatorId = AccountManager.getAccount().getPlatId();
        this.note = json.getString("note");
        this.needUpload = needUpload;
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
    public boolean needUpload() {
        return needUpload;
    }

    @Override
    public void setNeedUpload(boolean needUpload) {
        this.needUpload = needUpload;
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = new JSONObject();
        json.put("ver", ver);
        json.put("createTime", createTime);
        json.put("devAddress", devAddress);
        json.put("creatorPlat", creatorPlat);
        json.put("creatorId", creatorId);
        json.put("note", note);
        return json;
    }

    @Override
    public boolean setDataFromJson(JSONObject json) throws JSONException {
        return false;
    }

    @Override
    public boolean noData() {
        return true;
    }

    @Override
    public String toString() {
        return id + "-" + type + "-" + ver + "-" + createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId + "-" + note + "-" + needUpload;
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
