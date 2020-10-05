package com.cmtech.android.bledevice.record;

import android.content.Context;
import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.vise.log.ViseLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.List;

import static com.cmtech.android.bledevice.record.RecordType.ALL;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.RECORD_CMD_DOWNLOAD;
import static com.cmtech.android.bledevice.report.EcgReport.DEFAULT_VER;
import static com.cmtech.android.bledevice.report.EcgReport.INVALID_TIME;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.RETURN_CODE_WEB_FAILURE;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.RETURN_CODE_SUCCESS;

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
    public static final String QUERY_STR = "createTime, devAddress, ver, creatorPlat, creatorId, note, needUpload"; // used to create from local DB

    private int id;
    @Column(ignore = true)
    private RecordType type; // record type
    private long createTime; // create time
    private String devAddress; // device address
    private String ver; // record version
    private String creatorPlat; // creator plat name
    private String creatorId; // creator plat ID
    private String note; // note
    private int recordSecond; // unit: s
    private boolean needUpload; // need uploaded

    BasicRecord(RecordType type) {
        this.type = type;
        createTime = INVALID_TIME;
        devAddress = "";
        ver = DEFAULT_VER;
        creatorPlat = "";
        creatorId = "";
        note = "";
        recordSecond = 0;
        needUpload = true;
    }

    BasicRecord(long createTime, String devAddress, Account creator, String note) {
        this(ALL, createTime, devAddress, DEFAULT_VER, creator, note, true);
    }

    BasicRecord(RecordType type, long createTime, String devAddress, String ver, Account creator, String note, boolean needUpload) {
        if(creator == null) {
            throw new IllegalArgumentException("The creator of the record is null.");
        }
        this.type = type;
        this.createTime = createTime;
        this.devAddress = devAddress;
        this.ver = ver;
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
        this.note = note;
        this.recordSecond = 0;
        this.needUpload = needUpload;
    }

    BasicRecord(JSONObject json, boolean needUpload) throws JSONException{
        if(json == null) {
            throw new IllegalArgumentException("The json object is null.");
        }
        this.type = RecordType.fromCode(json.getInt("recordTypeCode"));
        this.createTime = json.getLong("createTime");
        this.devAddress = json.getString("devAddress");
        this.ver = json.getString("ver");
        this.creatorPlat = MyApplication.getAccount().getPlatName();
        this.creatorId = MyApplication.getAccount().getPlatId();
        this.note = json.getString("note");
        this.recordSecond = json.getInt("recordSecond");
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
    public String getName() {
        return createTime + devAddress;
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
    public String getCreatorPlat() {
        return creatorPlat;
    }

    @Override
    public String getCreatorId() {
        return creatorId;
    }

    public void setCreator(Account creator) {
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
    }

    @Override
    public String getCreatorName() {
        Account account = LitePal.where("platName = ? and platId = ?", creatorPlat, creatorId).findFirst(Account.class);
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
    public int getRecordSecond() {
        return recordSecond;
    }

    @Override
    public void setRecordSecond(int recordSecond) {
        this.recordSecond = recordSecond;
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
    public void fromJson(JSONObject json) {
        try {
            if(json.has("ver")) {
                ver = json.getString("ver");
            } else {
                ver = DEFAULT_VER;
            }
            creatorPlat = json.getString("creatorPlat");
            creatorId = json.getString("creatorId");
            note = json.getString("note");
            recordSecond = json.getInt("recordSecond");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("recordTypeCode", type.getCode());
            json.put("createTime", createTime);
            json.put("devAddress", devAddress);
            json.put("ver", ver);
            json.put("creatorPlat", creatorPlat);
            json.put("creatorId", creatorId);
            json.put("note", note);
            json.put("recordSecond", recordSecond);
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean noSignal() {
        return true;
    }

    @Override
    public void query(Context context, long fromTime, String queryStr, int num, IWebCallback callback) {
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_DOWNLOAD_BASIC_INFO, new Object[]{num, queryStr, fromTime}, new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if(code != RETURN_CODE_WEB_FAILURE && result != null) {
                    try {
                        JSONArray jsonArr = (JSONArray) result;
                        for (int i = 0; i < jsonArr.length(); i++) {
                            JSONObject json = (JSONObject) jsonArr.get(i);
                            BasicRecord newRecord = (BasicRecord) RecordFactory.createFromJson(json);
                            if (newRecord != null) {
                                newRecord.saveIfNotExist("createTime = ? and devAddress = ?", "" + newRecord.getCreateTime(), newRecord.getDevAddress());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                List<? extends IRecord> records = RecordFactory.createBasicRecordsFromLocalDb(RecordType.fromCode(getTypeCode()), MyApplication.getAccount(), fromTime, queryStr, num);

                int resultCode = SUCCESS;
                if(records == null || records.isEmpty()) {
                    callback.onFinish(resultCode, null);
                } else  {
                    callback.onFinish(resultCode, records);
                }
            }
        }).execute(this);
    }

    @Override
    public void upload(Context context, IWebCallback callback) {
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_QUERY, new IWebCallback() {
            @Override
            public void onFinish(int code, final Object result) {
                if (code == RETURN_CODE_SUCCESS) {
                    int id = (Integer) result;
                    if (id == INVALID_ID) {
                        ViseLog.e("uploading");
                        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_UPLOAD, false, new IWebCallback() {
                            @Override
                            public void onFinish(int code, Object result) {
                                if (code == RETURN_CODE_SUCCESS) {
                                    setNeedUpload(false);
                                    save();
                                    result = "上传成功";
                                }
                                callback.onFinish(code, result);
                            }
                        }).execute(BasicRecord.this);
                    } else {
                        ViseLog.e("updating note");
                        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_UPDATE_NOTE, false, new IWebCallback() {
                            @Override
                            public void onFinish(int code, Object result) {
                                if (code == RETURN_CODE_SUCCESS) {
                                    setNeedUpload(false);
                                    save();
                                    result = "更新成功";
                                }
                                callback.onFinish(code, result);
                            }
                        }).execute(BasicRecord.this);
                    }
                } else {
                    callback.onFinish(code, result);
                }
            }
        }).execute(this);
    }

    @Override
    public void download(Context context, IWebCallback callback) {
        new RecordWebAsyncTask(context, RECORD_CMD_DOWNLOAD, new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if (code == RETURN_CODE_SUCCESS) {
                    JSONObject json = (JSONObject) result;
                    fromJson(json);
                    save();
                    result = "下载成功";
                }
                callback.onFinish(code, result);
            }
        }).execute(this);
    }

    @Override
    public void delete(Context context, IWebCallback callback) {
        Class<? extends BasicRecord> recordClass = getClass();
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_DELETE, new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if(code == RETURN_CODE_SUCCESS) {
                    LitePal.delete(recordClass, getId());
                    result = "删除成功";
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
}
