package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.ECG;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_HR;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.asynctask.ReportAsyncTask;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmDetectResult;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmDetectResultItem;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.IEcgRhythmDetector;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.MyEcgRhythmDetector;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.util.ListStringUtil;
import com.cmtech.android.bledeviceapp.util.UploadDownloadFileUtil;
import com.vise.utils.file.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      BleEcgRecord
 * Description:    心电信号记录类
 * Author:         chenm
 * CreateDate:     2020/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午7:11
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleEcgRecord extends BasicRecord implements ISignalRecord, IDiagnosable, Serializable {
    //-----------------------------------------常量
    // 记录每个数据的字节数
    private static final int BYTES_PER_DATUM = 2;

    // 采样率
    private int sampleRate = 0;

    // 1mV标定值
    private int caliValue = 0;

    // 导联码
    private int leadTypeCode = 0;

    // 平均心率值
    private int aveHr = INVALID_HR;

    // 心电采集时中断的位置值列表
    private final List<Integer> breakPos = new ArrayList<>();

    // 心电采集时中断的位置对应的时刻点列表
    private final List<Long> breakTime = new ArrayList<>();

    // 采集是否中断
    @Column(ignore = true)
    private boolean interrupt = false;

    @Column(ignore = true)
    private EcgRhythmDetectResult rhythmDetectResult = new EcgRhythmDetectResult("1.1");

    // 由RecordFactory工厂类通过反射调用来创建对象
    private BleEcgRecord(String ver, long createTime, String devAddress, int creatorId) {
        super(ECG, ver, createTime, devAddress, creatorId);
    }

    // 创建信号文件
    public void createSigFile() {
        super.createSigFile(BYTES_PER_DATUM);
    }

    // 打开信号文件
    public void openSigFile() {
        super.openSigFile(BYTES_PER_DATUM);
    }

    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        try {
            JSONObject contentJson = new JSONObject(getReportContent());
            String ver = contentJson.getString("ver");
            rhythmDetectResult = new EcgRhythmDetectResult(ver);
            rhythmDetectResult.fromJson(contentJson);
         } catch (JSONException ex) {
            ex.printStackTrace();
        }
        sampleRate = json.getInt("sampleRate");
        caliValue = json.getInt("caliValue");
        leadTypeCode = json.getInt("leadTypeCode");
        aveHr = json.getInt("aveHr");
        if(json.has("breakPos"))
            ListStringUtil.stringToList(json.getString("breakPos"), breakPos, Integer.class);
        if(json.has("breakTime"))
            ListStringUtil.stringToList(json.getString("breakTime"), breakTime, Long.class);
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("caliValue", caliValue);
        json.put("leadTypeCode", leadTypeCode);
        json.put("aveHr", aveHr);
        json.put("breakPos", ListStringUtil.listToString(breakPos));
        json.put("breakTime", ListStringUtil.listToString(breakTime));
        return json;
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public int getCaliValue() {
        return caliValue;
    }

    public void setCaliValue(int caliValue) {
        this.caliValue = caliValue;
    }

    public void setLeadTypeCode(int leadTypeCode) {
        this.leadTypeCode = leadTypeCode;
    }

    public int getAveHr() {
        return aveHr;
    }

    public void setAveHr(int aveHr) {
        this.aveHr = aveHr;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    public void updateReportContent() {
        try {
            setReportContent(rhythmDetectResult.toJson().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getReportContent() {
        return rhythmDetectResult.toString();
    }

    @Override
    public int readData() throws IOException {
        if(sigFile == null) throw new IOException();
        return sigFile.readShort();
    }

    // 获取当前数据位置对应的时间
    public long getCurrentPosTime() {
        if(sigFile == null)
            return -1;
        try {
            return getPosTime(sigFile.getCurrentPos());
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // 获取pos指定数据位置对应的时间点
    private long getPosTime(int pos) {
        int startPos;
        long startTime;
        if(breakPos.isEmpty()) {
            startPos = 0;
            startTime = getCreateTime();
        } else {
            int i;
            for (i = 0; i < breakPos.size(); i++) {
                if (breakPos.get(i) > pos) break;
            }
            startPos = breakPos.get(i-1);
            startTime = breakTime.get(i-1);
        }
        return startTime + (pos - startPos)*1000L/sampleRate;
    }

    /**
     * 处理一个ECG信号值
     * @param ecg ECG信号
     * @return 是否正常处理
     */
    public boolean record(short ecg) {
        boolean success = false;
        try {
            if(sigFile != null) {
                sigFile.writeShort(ecg);
                if (interrupt) {
                    breakPos.add(sigFile.size() - 1);
                    breakTime.add(new Date().getTime());
                    interrupt = false;
                }
                success = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public void addRhythmDetectResultItem(EcgRhythmDetectResultItem item) {
        rhythmDetectResult.addItem(item);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + leadTypeCode +
                "-" + breakPos + "-" + breakTime;
    }

    @Override
    public void remoteDiagnose(Context context, IWebResponseCallback callback) {
        new ReportAsyncTask(context, callback).execute(this);
    }

    @Override
    public void localDiagnose() {
        IEcgRhythmDetector algorithm = new MyEcgRhythmDetector();
        EcgReport rtnReport = algorithm.process(this);
        setReportVer(rtnReport.getVer());
        setReportProvider(rtnReport.getReportProvider());
        setReportTime(rtnReport.getReportTime());
        setReportContent(rtnReport.getReportContent());
        setReportStatus(rtnReport.getReportStatus());
        setAveHr(rtnReport.getAveHr());
        //setNeedUpload(true);
        save();
    }

    @Override
    public void download(Context context, ICodeCallback callback) {
        File file = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, getSigFileName());
        if(!file.exists()) {
            if(UploadDownloadFileUtil.isFileExist("ECG", getSigFileName())) {
                UploadDownloadFileUtil.downloadFile(context, "ECG", getSigFileName(), BasicRecord.SIG_FILE_PATH, new ICodeCallback() {
                    @Override
                    public void onFinish(int code) {
                        if(code==RETURN_CODE_SUCCESS) {
                            BleEcgRecord.super.download(context, callback);
                        } else {
                            callback.onFinish(RETURN_CODE_DOWNLOAD_ERR);
                        }
                    }
                });
            } else {
                callback.onFinish(RETURN_CODE_DOWNLOAD_ERR);
            }
        } else {
            super.download(context, callback);
        }
    }

    @Override
    public void upload(Context context, ICodeCallback callback) {
        File sigFile = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, getSigFileName());
        if(sigFile.exists()) {
            if(!UploadDownloadFileUtil.isFileExist("ECG", getSigFileName())) {
                UploadDownloadFileUtil.uploadFile(context, "ECG", sigFile, new ICodeCallback() {
                    @Override
                    public void onFinish(int code) {
                        if (code == RETURN_CODE_SUCCESS) {
                            BleEcgRecord.super.upload(context, callback);
                        } else {
                            callback.onFinish(RETURN_CODE_DOWNLOAD_ERR);
                        }
                    }
                });
            } else {
                super.upload(context, callback);
            }
        } else {
            callback.onFinish(RETURN_CODE_UPLOAD_ERR);
        }
    }

}
