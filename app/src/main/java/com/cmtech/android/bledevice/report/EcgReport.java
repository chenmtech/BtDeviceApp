package com.cmtech.android.bledevice.report;

import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.interfac.IJsonable;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.LitePalSupport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EcgReport extends LitePalSupport implements IJsonable {
    private int id;
    private String ver = "1.0";
    private long createTime = -1;
    private String content = "";

    public EcgReport() {

    }

    public int getId() {
        return id;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean fromJson(JSONObject json) {
        if(json == null) {
            return false;
        }

        try {
            ver = json.getString("ver");
            createTime = json.getLong("createTime");
            content = json.getString("content");
            return save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("ver", ver);
        json.put("createTime", createTime);
        json.put("content", content);
        return json;
    }

    @NonNull
    @Override
    public String toString() {
        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault());
        return "时间：" + dateFmt.format(new Date(createTime)) + "\n内容：" + content;
    }
}
