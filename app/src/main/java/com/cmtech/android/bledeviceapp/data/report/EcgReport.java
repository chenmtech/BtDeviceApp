package com.cmtech.android.bledeviceapp.data.report;

import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.interfac.IJsonable;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.LitePalSupport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.cmtech.android.bledevice.ecg.process.signal.EcgSignalProcessor.INVALID_HR;
import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.DEFAULT_RECORD_VER;

public class EcgReport extends LitePalSupport implements IJsonable {
    public static final long INVALID_TIME = -1;
    public static final String DEFAULT_REPORT_CONTENT = "无";
    public static final int DONE = 0;
    public static final int REQUEST = 1;
    public static final int PROCESS = 2;

    private int id;
    private String ver = DEFAULT_RECORD_VER;
    private long reportTime = INVALID_TIME;
    private String content = DEFAULT_REPORT_CONTENT;
    private int status = DONE;
    private int aveHr = INVALID_HR;

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

    public int getAveHr() {
        return aveHr;
    }

    public void setAveHr(int aveHr) {
        this.aveHr = aveHr;
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        if(json.has("reportVer"))
            ver = json.getString("reportVer");
        reportTime = json.getLong("reportTime");
        content = json.getString("content");
        status = json.getInt("status");
        if(json.has("aveHr"))
            aveHr = json.getInt("aveHr");
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = new JSONObject();
        json.put("reportVer", ver);
        json.put("reportTime", reportTime);
        json.put("content", content);
        json.put("status", status);
        json.put("aveHr", aveHr);
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
        return "时间：" + dateFmt.format(new Date(reportTime))
                + "\n平均心率：" + aveHr
                + "\n内容：" + content
                + "\n状态：" + statusStr;
    }
}
