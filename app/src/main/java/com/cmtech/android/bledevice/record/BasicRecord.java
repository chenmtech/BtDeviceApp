package com.cmtech.android.bledevice.record;

import android.content.Context;
import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebOperationCallback;
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
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_FAILURE;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_SUCCESS;

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
    public static final String QUERY_STR = "ver, createTime, devAddress, creatorPlat, creatorId, note, needUpload"; // used to create from local DB

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

    BasicRecord(long createTime, String devAddress, Account creator, String note) {
        this(ALL, "1.0", createTime, devAddress, creator, note, true);
    }

    BasicRecord(RecordType type, String ver, long createTime, String devAddress, Account creator, String note, boolean needUpload) {
        if(creator == null) {
            throw new IllegalArgumentException("The creator of the record is null.");
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

    BasicRecord(JSONObject json, boolean needUpload) throws JSONException{
        if(json == null) {
            throw new IllegalArgumentException("The json object is null.");
        }
        this.ver = json.getString("ver");
        this.type = RecordType.fromCode(json.getInt("recordTypeCode"));
        this.createTime = json.getLong("createTime");
        this.devAddress = json.getString("devAddress");
        this.creatorPlat = MyApplication.getAccount().getPlatName();
        this.creatorId = MyApplication.getAccount().getPlatId();
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
    public boolean needUpload() {
        return needUpload;
    }

    @Override
    public void setNeedUpload(boolean needUpload) {
        this.needUpload = needUpload;
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("ver", ver);
            json.put("recordTypeCode", type.getCode());
            json.put("createTime", createTime);
            json.put("devAddress", devAddress);
            json.put("creatorPlat", creatorPlat);
            json.put("creatorId", creatorId);
            json.put("note", note);
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void fromJson(JSONObject json) {
        return;
    }

    @Override
    public boolean noSignal() {
        return true;
    }

    @Override
    public void query(Context context, long fromTime, String queryStr, int num, IWebOperationCallback callback) {
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_DOWNLOAD_BASIC_INFO, new Object[]{num, queryStr, fromTime}, new IWebOperationCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if(code != WEB_CODE_FAILURE && result != null) {
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
    public void upload(Context context, IWebOperationCallback callback) {
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_QUERY, new IWebOperationCallback() {
            @Override
            public void onFinish(int code, final Object rlt) {
                final int[] resultCode = {FAILURE};
                final String[] resultStr = {"网络错误"};
                final boolean result = (code == WEB_CODE_SUCCESS);
                if (result) {
                    int id = (Integer) rlt;
                    if (id == INVALID_ID) {
                        ViseLog.e("uploading");
                        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_UPLOAD, false, new IWebOperationCallback() {
                            @Override
                            public void onFinish(int code, Object result) {
                                if (code == WEB_CODE_SUCCESS) {
                                    setNeedUpload(false);
                                    save();
                                    resultCode[0] = SUCCESS;
                                    resultStr[0] = "上传成功";
                                }
                                callback.onFinish(resultCode[0], resultStr[0]);
                            }
                        }).execute(BasicRecord.this);
                    } else {
                        ViseLog.e("updating note");
                        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_UPDATE_NOTE, false, new IWebOperationCallback() {
                            @Override
                            public void onFinish(int code, Object result) {
                                if (code == WEB_CODE_SUCCESS) {
                                    setNeedUpload(false);
                                    save();
                                    resultCode[0] = SUCCESS;
                                    resultStr[0] = "更新成功";
                                }
                                callback.onFinish(resultCode[0], resultStr[0]);
                            }
                        }).execute(BasicRecord.this);
                    }
                } else {
                    callback.onFinish(resultCode[0], resultStr[0]);
                }
            }
        }).execute(this);
    }

    @Override
    public void download(Context context, IWebOperationCallback callback) {
        new RecordWebAsyncTask(context, RECORD_CMD_DOWNLOAD, new IWebOperationCallback() {
            @Override
            public void onFinish(int code, Object result) {
                int resultCode = FAILURE;
                String resultStr = "网络错误";
                if (code == WEB_CODE_SUCCESS) {
                    JSONObject json = (JSONObject) result;
                    fromJson(json);
                    save();
                    resultCode = SUCCESS;
                    resultStr = "下载成功";
                }

                callback.onFinish(resultCode, resultStr);
            }
        }).execute(this);
    }

    @Override
    public void delete(Context context, IWebOperationCallback callback) {
        Class<? extends BasicRecord> recordClass = getClass();
        new RecordWebAsyncTask(context, RecordWebAsyncTask.RECORD_CMD_DELETE, new IWebOperationCallback() {
            @Override
            public void onFinish(int code, Object result) {
                LitePal.delete(recordClass, getId());
                callback.onFinish(SUCCESS, null);
            }
        }).execute(this);
    }

    @NonNull
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
