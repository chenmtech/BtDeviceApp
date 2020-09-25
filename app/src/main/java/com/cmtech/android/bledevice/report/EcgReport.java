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
    private static final int DONE = 0;
    private static final int REQUEST = 1;
    private static final int PROCESS = 2;
    private int id;
    private String ver = "1.0";
    private long reportTime = -1;
    private String content = "";
    private int status = DONE;

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

    public long getReportTime() {
        return reportTime;
    }

    public void setReportTime(long reportTime) {
        this.reportTime = reportTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean fromJson(JSONObject json) {
        if(json == null) {
            return false;
        }

        try {
            ver = json.getString("reportVer");
            reportTime = json.getLong("reportTime");
            content = json.getString("content");
            status = json.getInt("status");
            return save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("reportVer", ver);
        json.put("reportTime", reportTime);
        json.put("content", content);
        return json;
    }

    @NonNull
    @Override
    public String toString() {
        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String statusStr = "未知";
        switch (status) {
            case DONE:
                statusStr = "已处理";
                break;
            case REQUEST:
                statusStr = "等待处理";
                break;
            case PROCESS:
                statusStr = "正在处理";
                break;
            default:
                break;
        }
        return "报告时间：" + dateFmt.format(new Date(reportTime))
                + "\n内容：" + content
                + "\n状态：" + statusStr;
    }
}
