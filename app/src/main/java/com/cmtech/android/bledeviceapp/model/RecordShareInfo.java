package com.cmtech.android.bledeviceapp.model;

import android.content.Context;

import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.LitePalSupport;

public class RecordShareInfo extends LitePalSupport implements IJsonable, IWebOperation {
    //-----------------------------------------实例变量

    // ID号
    private int id;

    private int fromId;

    private int toId;

    private String fromUserName;

    private String toUserName;

    RecordShareInfo(int fromId, int toId, String fromUserName, String toUserName) {
        this.fromId = fromId;
        this.toId = toId;
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
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

    @Override
    public void fromJson(JSONObject json) throws JSONException {

    }

    @Override
    public JSONObject toJson() throws JSONException {
        return null;
    }

    @Override
    public void upload(Context context, ICodeCallback callback) {

    }

    @Override
    public void download(Context context, ICodeCallback callback) {

    }

    @Override
    public void delete(Context context, ICodeCallback callback) {
        return;
    }
}
