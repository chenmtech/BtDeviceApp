package com.cmtech.android.bledeviceapp.data.record;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.asynctask.RecordAsyncTask;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.WebResponse;

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

import static com.cmtech.android.bledeviceapp.asynctask.RecordAsyncTask.CMD_DOWNLOAD_RECORD;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
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
    public static final String DEFAULT_RECORD_VER = "1.0";
    private static final String[] basicItems = {"id", "createTime", "devAddress",
            "creatorId", "ver", "note", "recordSecond", "needUpload"};

    private int id;
    @Column(ignore = true)
    private final RecordType type; // record type
    private long createTime; // create time
    private final String devAddress; // device address
    private String ver = DEFAULT_RECORD_VER; // record version
    private int creatorId = INVALID_ID; // creator plat ID

    private String note = ""; // note
    private int recordSecond = 0; // unit: s
    private boolean needUpload = true; // need uploaded

    BasicRecord(RecordType type, String ver, long createTime, String devAddress, int creatorId) {
        this.type = type;
        this.ver = ver;
        this.createTime = createTime;
        this.devAddress = devAddress;
        this.creatorId = creatorId;
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

    public int getCreatorId() {
        return creatorId;
    }

    public String getCreatorNickName() {
        Account account = MyApplication.getAccount();
        if(account == null || account.getAccountId() != creatorId) {
            return  "匿名";
        } else {
            return account.getNickNameOrUserName();
        }
    }

    public String getCreatorNickNameInOutputReport() {
        Account account = MyApplication.getAccount();
        if(account == null || account.getAccountId() != creatorId) {
            return  "匿名";
        } else {
            String nickName = account.getNickName();
            String userName = account.getUserName();
            if("".equals(nickName)) {
                int length = userName.length();
                return userName.substring(0, length/3) + "*" + userName.substring(length*2/3, length);
            }
            return nickName;
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
        note = json.getString("note");
        recordSecond = json.getInt("recordSecond");
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = new JSONObject();
        json.put("recordTypeCode", type.getCode());
        json.put("createTime", createTime);
        json.put("devAddress", devAddress);
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
    public final void retrieveList(Context context, int num, String queryStr, long fromTime, ICodeCallback callback) {
        new RecordAsyncTask(context, "获取记录中，请稍等。", RecordAsyncTask.CMD_DOWNLOAD_RECORD_LIST, new Object[]{num, queryStr, fromTime}, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                JSONArray jsonArr = (JSONArray) response.getContent();
                if(code == RETURN_CODE_SUCCESS && jsonArr != null) {
                    for (int i = 0; i < jsonArr.length(); i++) {
                        try {
                            JSONObject json = (JSONObject) jsonArr.get(i);
                            if(json == null) continue;
                            RecordType type = RecordType.fromCode(json.getInt("recordTypeCode"));
                            String ver = json.getString("ver");
                            long createTime = json.getLong("createTime");
                            String devAddress = json.getString("devAddress");
                            int creatorId = json.getInt("creatorId");
                            BasicRecord record = RecordFactory.create(type, ver, createTime, devAddress, creatorId);
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
                callback.onFinish(code);
            }
        }).execute(this);
    }

    @Override
    public final void upload(Context context, ICodeCallback callback) {
        new RecordAsyncTask(context, "上传记录中，请稍等。", RecordAsyncTask.CMD_UPLOAD_RECORD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if(code == RETURN_CODE_SUCCESS) {
                    setNeedUpload(false);
                    save();
                }
                callback.onFinish(code);
            }
        }).execute(this);
    }

    @Override
    public final void download(Context context, ICodeCallback callback) {
        new RecordAsyncTask(context, "下载记录中，请稍等。", CMD_DOWNLOAD_RECORD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if (code == RETURN_CODE_SUCCESS) {
                    JSONObject content = (JSONObject) response.getContent();
                    if(content != null) {
                        try {
                            fromJson(content);
                            save();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = RETURN_CODE_DATA_ERR;
                        }
                    }
                }
                callback.onFinish(code);
            }
        }).execute(this);
    }

    @Override
    public final void delete(Context context, ICodeCallback callback) {
        Class<? extends BasicRecord> recordClass = getClass();
        new RecordAsyncTask(context, "删除记录中，请稍等。", RecordAsyncTask.CMD_DELETE_RECORD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if(code == RETURN_CODE_SUCCESS) {

                }
                LitePal.delete(recordClass, getId());
                callback.onFinish(code);
            }
        }).execute(this);
    }

    @NonNull
    @Override
    public String toString() {
        return id + "-" + type + "-" + ver + "-" + createTime + "-" + devAddress + "-" + creatorId + "-" + note + "-" + recordSecond + "-" + needUpload;
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


    public static List<? extends BasicRecord> retrieveListFromLocalDb(RecordType type, Account creator, long fromTime, String noteFilterStr, int num) {
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
                //ViseLog.e(recordClass);
                if(TextUtils.isEmpty(noteFilterStr)) {
                    records.addAll(LitePal.select(basicItems)
                            .where("creatorId = ? and createTime < ?", ""+creator.getAccountId(), ""+fromTime)
                            .order("createTime desc").limit(num).find(recordClass, true));
                } else {
                    records.addAll(LitePal.select(basicItems)
                            .where("creatorId = ? and createTime < ? and note like ?", ""+creator.getAccountId(), ""+fromTime, "%"+noteFilterStr+"%")
                            .order("createTime desc").limit(num).find(recordClass, true));
                }
                //ViseLog.e(records);
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
