package com.cmtech.android.bledevice.record;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.model.Account;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.RECORD_CMD_DOWNLOAD;
import static com.cmtech.android.bledevice.report.EcgReport.DEFAULT_VER;

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
public abstract class BasicRecord extends LitePalSupport implements IJsonable, IWebOperation {
    public static final int INVALID_ID = -1;

    private int id;
    @Column(ignore = true)
    private final RecordType type; // record type
    private long createTime; // create time
    private final String devAddress; // device address
    private String ver = DEFAULT_VER; // record version
    private final String creatorPlat; // creator plat name
    private final String creatorId; // creator plat ID


    private String note = ""; // note
    private int recordSecond = 0; // unit: s
    private boolean needUpload = true; // need uploaded

    BasicRecord(RecordType type, long createTime, String devAddress, Account creator) {
        this.type = type;
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
        this.ver= ver;
    }

    public int getTypeCode() {
        return type.getCode();
    }

    public String getName() {
        return createTime + devAddress;
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

    public String getCreatorPlat() {
        return creatorPlat;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getCreatorName() {
        Account account = LitePal.where("platName = ? and platId = ?", creatorPlat, creatorId).findFirst(Account.class);
        if(account == null)
            return creatorId;
        else {
            return account.getName();
        }
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getRecordSecond() {
        return recordSecond;
    }

    public void setRecordSecond(int recordSecond) {
        this.recordSecond = recordSecond;
    }

    public boolean needUpload() {
        return needUpload;
    }

    public void setNeedUpload(boolean needUpload) {
        this.needUpload = needUpload;
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        basicRecordFromJson(json);
    }

    public void basicRecordFromJson(JSONObject json) throws JSONException{
        //ver = json.getString("ver");
        note = json.getString("note");
        recordSecond = json.getInt("recordSecond");
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = new JSONObject();
        json.put("recordTypeCode", type.getCode());
        json.put("createTime", createTime);
        json.put("devAddress", devAddress);
        json.put("creatorPlat", creatorPlat);
        json.put("creatorId", creatorId);
        json.put("ver", ver);
        json.put("note", note);
        json.put("recordSecond", recordSecond);
        return json;
    }

    public boolean noSignal() {
        return true;
    }

    @Override
    public final void retrieveList(Context context, int num, String queryStr, long fromTime, IWebCallback callback) {
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_DOWNLOAD_LIST, new Object[]{num, queryStr, fromTime}, new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                JSONArray jsonArr = (JSONArray) result;
                if(code == RETURN_CODE_SUCCESS && jsonArr != null) {
                    for (int i = 0; i < jsonArr.length(); i++) {
                        try {
                            JSONObject json = (JSONObject) jsonArr.get(i);
                            if(json == null) continue;
                            RecordType type = RecordType.fromCode(json.getInt("recordTypeCode"));
                            long createTime = json.getLong("createTime");
                            String devAddress = json.getString("devAddress");
                            String creatorPlat = json.getString("creatorPlat");
                            String creatorId = json.getString("creatorId");
                            BasicRecord record = RecordFactory.create(type, createTime, devAddress, new Account(creatorPlat, creatorId, "", "", ""));
                            if (record != null) {
                                record.basicRecordFromJson(json);
                                record.setNeedUpload(false);
                                record.saveIfNotExist("createTime = ? and devAddress = ?", "" + record.getCreateTime(), record.getDevAddress());
                            }
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                callback.onFinish(code, result);
            }
        }).execute(this);
    }

    @Override
    public final void upload(Context context, IWebCallback callback) {
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_UPLOAD, new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if(code == RETURN_CODE_SUCCESS) {
                    setNeedUpload(false);
                    save();
                }
                callback.onFinish(code, result);
            }
        }).execute(this);
    }

    @Override
    public final void download(Context context, IWebCallback callback) {
        new RecordWebAsyncTask(context, RECORD_CMD_DOWNLOAD, new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if (code == RETURN_CODE_SUCCESS) {
                    JSONObject json = (JSONObject) result;
                    if(json != null) {
                        try {
                            fromJson(json);
                            save();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = RETURN_CODE_OTHER_ERR;
                        }
                    }
                }
                callback.onFinish(code, result);
            }
        }).execute(this);
    }

    @Override
    public final void delete(Context context, IWebCallback callback) {
        Class<? extends BasicRecord> recordClass = getClass();
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_DELETE, new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if(code == RETURN_CODE_SUCCESS) {
                    LitePal.delete(recordClass, getId());
                }
                callback.onFinish(code, result);
            }
        }).execute(this);
    }

    @NonNull
    @Override
    public String toString() {
        return id + "-" + type + "-" + ver + "-" + createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId + "-" + note + "-" + recordSecond + "-" + needUpload;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        BasicRecord other = (BasicRecord) otherObject;
        return getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }


    public static List<? extends BasicRecord> retrieveFromLocalDb(RecordType type, Account creator, long fromTime, String noteFilterStr, int num) {
        List<RecordType> types = new ArrayList<>();
        if(type == RecordType.ALL) {
            for(RecordType t : RecordType.values()) {
                if(t != RecordType.ALL) {
                    types.add(t);
                }
            }
        }
        else
            types.add(type);

        List<BasicRecord> records = new ArrayList<>();
        for(RecordType t : types) {
            Class<? extends BasicRecord> recordClass = t.getRecordClass();
            if (recordClass != null) {
                if(TextUtils.isEmpty(noteFilterStr)) {
                    records.addAll(LitePal.where("creatorPlat = ? and creatorId = ? and createTime < ?", creator.getPlatName(), creator.getPlatId(), "" + fromTime)
                            .order("createTime desc").limit(num).find(recordClass, true));
                } else {
                    records.addAll(LitePal.where("creatorPlat = ? and creatorId = ? and createTime < ? and note like ?", creator.getPlatName(), creator.getPlatId(), "" + fromTime, "%"+noteFilterStr+"%")
                            .order("createTime desc").limit(num).find(recordClass, true));
                }
            }
        }
        if(records.isEmpty()) return null;
        Collections.sort(records, new Comparator<BasicRecord>() {
            @Override
            public int compare(BasicRecord o1, BasicRecord o2) {
                int rlt = 0;
                if(o2.getCreateTime() > o1.getCreateTime()) rlt = 1;
                else if(o2.getCreateTime() < o1.getCreateTime()) rlt = -1;
                return rlt;
            }
        });
        return records.subList(0, Math.min(records.size(), num));
    }
}
