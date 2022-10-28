package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.bledeviceapp.interfac.IJsonable;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.LitePalSupport;

public class ShareInfo extends LitePalSupport implements IJsonable {
    public static final int DENY = 0;
    public static final int WAITING = 1;
    public static final int AGREE = 2;

    //-----------------------------------------实例变量

    // ID号
    private int id;

    private int fromId;

    private int toId;

    private String fromUserName;

    private String toUserName;

    private int status;

    public ShareInfo() {

    }

    public ShareInfo(int fromId, int toId, String fromUserName, String toUserName, int status) {
        this.fromId = fromId;
        this.toId = toId;
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
        this.status = status;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException {
        fromId = json.getInt("fromId");
        toId = json.getInt("toId");
        fromUserName = json.getString("fromUserName");
        toUserName = json.getString("toUserName");
        status = json.getInt("status");
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("fromId", fromId);
        json.put("toId", toId);
        json.put("fromUserName", fromUserName);
        json.put("toUserName", toUserName);
        json.put("status", status);
        return json;
    }

    public String toString() {
        return fromId + fromUserName + "->" + toId + toUserName +":" + status;
    }
}
