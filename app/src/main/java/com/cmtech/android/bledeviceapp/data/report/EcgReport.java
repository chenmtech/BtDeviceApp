package com.cmtech.android.bledeviceapp.data.report;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_HR;
import static com.cmtech.android.bledeviceapp.util.DateTimeUtil.INVALID_TIME;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.interfac.IJsonable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EcgReport implements IJsonable {
    public static final String DEFAULT_REPORT_CONTENT = "无";
    public static final String DEFAULT_REPORT_VER = "1.0";
    public static final int LOCAL = 0;
    public static final int REMOTE = 1;
    public static final int DONE = 0;
    public static final int REQUEST = 1;
    public static final int PROCESS = 2;
    public static final int WAIT_READ = 3;

    private String ver = DEFAULT_REPORT_VER;
    private int reportClient = LOCAL; // 产生报告的终端：本地或云端
    private long reportTime = INVALID_TIME;
    private String reportContent = DEFAULT_REPORT_CONTENT;
    private int reportStatus = DONE;
    private int aveHr = INVALID_HR;

    public EcgReport() {
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public int getReportClient() {
        return reportClient;
    }

    public void setReportClient(int reportClient) {
        this.reportClient = reportClient;
    }

    public long getReportTime() {
        return reportTime;
    }

    public void setReportTime(long reportTime) {
        this.reportTime = reportTime;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    public int getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(int reportStatus) {
        this.reportStatus = reportStatus;
    }

    public int getAveHr() {
        return aveHr;
    }

    public void setAveHr(int aveHr) {
        this.aveHr = aveHr;
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        ver = json.getString("reportVer");
        reportClient = json.getInt("reportClient");
        reportTime = json.getLong("reportTime");
        reportContent = json.getString("reportContent");
        reportStatus = json.getInt("reportStatus");
        if(json.has("aveHr"))
            aveHr = json.getInt("aveHr");
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = new JSONObject();
        json.put("reportVer", ver);
        json.put("reportClient", reportClient);
        json.put("reportTime", reportTime);
        json.put("reportContent", reportContent);
        json.put("reportStatus", reportStatus);
        json.put("aveHr", aveHr);
        return json;
    }

    @NonNull
    @Override
    public String toString() {
        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String statusStr = "未知";
        switch (reportStatus) {
            case DONE:
                statusStr = "已处理";
                break;
            case REQUEST:
                statusStr = "等待处理";
                break;
            case PROCESS:
                statusStr = "正在处理";
                break;
            case WAIT_READ:
                statusStr = "等待读取";
                break;
            default:
                break;
        }
        return "时间：" + dateFmt.format(new Date(reportTime))
                + "\n报告端：" + ((reportClient == LOCAL) ? "本地端" : "云端")
                + "\n平均心率：" + aveHr
                + "\n内容：" + reportContent
                + "\n状态：" + statusStr;
    }
}
