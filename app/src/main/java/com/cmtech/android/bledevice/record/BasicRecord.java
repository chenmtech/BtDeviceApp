package com.cmtech.android.bledevice.record;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IDbOperation;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.model.Account;
import com.vise.log.ViseLog;

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
import static com.cmtech.android.bledevice.report.EcgReport.INVALID_TIME;

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
        if(creator == null) {
            throw new IllegalArgumentException("The creator of the record is null.");
        }
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
    public void fromJson(JSONObject json) {
        basicFromJson(json);
    }

    public void basicFromJson(JSONObject json) {
        try {
            ver = json.getString("ver");
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
            json.put("creatorPlat", creatorPlat);
            json.put("creatorId", creatorId);
            json.put("ver", ver);
            json.put("note", note);
            json.put("recordSecond", recordSecond);
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean noSignal() {
        return true;
    }

    @Override
    public void retrieveRecordList(Context context, long fromTime, String queryStr, int num, IWebCallback callback) {
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_DOWNLOAD_LIST, new Object[]{num, queryStr, fromTime}, new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if(code == RETURN_CODE_SUCCESS) {
                    try {
                        JSONArray jsonArr = (JSONArray) result;
                        for (int i = 0; i < jsonArr.length(); i++) {
                            JSONObject json = (JSONObject) jsonArr.get(i);
                            RecordType type = RecordType.fromCode(json.getInt("recordTypeCode"));
                            long createTime = json.getLong("createTime");
                            String devAddress = json.getString("devAddress");
                            String creatorPlat = json.getString("creatorPlat");
                            String creatorId = json.getString("creatorId");
                            BasicRecord record = RecordFactory.create(type, createTime, devAddress, new Account(creatorPlat, creatorId, "", "", ""));
                            if(record != null) {
                                record.basicFromJson(json);
                                record.setNeedUpload(false);
                                record.saveIfNotExist("createTime = ? and devAddress = ?", "" + record.getCreateTime(), record.getDevAddress());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                List<? extends BasicRecord> records = BasicRecord.retrieveRecordsFromLocalDb(RecordType.fromCode(getTypeCode()), MyApplication.getAccount(), fromTime, queryStr, num);

                if(records == null || records.isEmpty()) {
                    callback.onFinish(RETURN_CODE_SUCCESS, null);
                } else  {
                    callback.onFinish(RETURN_CODE_SUCCESS, records);
                }
            }
        }).execute(this);
    }

    @Override
    public void upload(Context context, IWebCallback callback) {
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_QUERY_ID, new IWebCallback() {
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
                    ViseLog.e(json);
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


    public static List<? extends BasicRecord> retrieveRecordsFromLocalDb(RecordType type, Account creator, long fromTime, String noteFilterStr, int num) {
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
                try {
                    if(TextUtils.isEmpty(noteFilterStr)) {
                        records.addAll(LitePal.where("creatorPlat = ? and creatorId = ? and createTime < ?", creator.getPlatName(), creator.getPlatId(), "" + fromTime)
                                .order("createTime desc").limit(num).find(recordClass, true));
                    } else {
                        records.addAll(LitePal.where("creatorPlat = ? and creatorId = ? and createTime < ? and note like ?", creator.getPlatName(), creator.getPlatId(), "" + fromTime, "%"+noteFilterStr+"%")
                                .order("createTime desc").limit(num).find(recordClass, true));
                    }
                } catch (Exception e) {
                    ViseLog.e(e);
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
