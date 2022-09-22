package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.ECG;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_HR;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.asynctask.ReportAsyncTask;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.IEcgArrhythmiaDetector;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.MyEcgArrhythmiaDetector;
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

    @Column(ignore = true)
    private RecordFile sigFile;

    // 采集是否中断
    @Column(ignore = true)
    private boolean interrupt = false;

    // 由RecordFactory工厂类通过反射调用来创建对象
    private BleEcgRecord(String ver, long createTime, String devAddress, int creatorId) {
        super(ECG, ver, createTime, devAddress, creatorId);
    }

    // 创建信号文件
    public void createSigFile() {
        try {
            sigFile = new RecordFile(getSigFileName(), "c");
        } catch (IOException e) {
            e.printStackTrace();
            sigFile = null;
        }
    }

    // 打开信号文件
    public void openSigFile() {
        try {
            sigFile = new RecordFile(getSigFileName(), "o");
        } catch (IOException e) {
            e.printStackTrace();
            sigFile = null;
        }
    }

    // 关闭信号文件
    public void closeSigFile() {
        if(sigFile != null) {
            try {
                sigFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
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

    // 当前记录是否有信号
    @Override
    public boolean noSignal() {
        return (sigFile==null);
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

    // 是否到达信号末尾
    @Override
    public boolean isEOD() {
        if(sigFile != null) {
            try {
                return sigFile.isEof();
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void seekData(int pos) {
        if(sigFile!= null) {
            try {
                sigFile.seekData(pos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int readData() throws IOException {
        if(sigFile == null) throw new IOException();
        return sigFile.readData();
    }

    @Override
    public int getDataNum() {
        if(sigFile == null) return 0;
        return sigFile.size();
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
     * @param ecg
     * @return
     */
    public boolean process(short ecg) {
        boolean success = false;
        try {
            if(sigFile != null) {
                sigFile.writeData(ecg);
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
        IEcgArrhythmiaDetector algorithm = new MyEcgArrhythmiaDetector();
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
        boolean success = true;
        File file = FileUtil.getFile(BasicRecord.SIG_PATH, getSigFileName());
        if(!file.exists())
            success = UploadDownloadFileUtil.downloadFile(context, "ECG", getSigFileName(), BasicRecord.SIG_PATH);

        if(success) {
            super.download(context, callback);
        } else {
            callback.onFinish(RETURN_CODE_UPLOAD_ERR);
        }
    }

    @Override
    public void upload(Context context, ICodeCallback callback) {
        boolean success = true;
        File sigFile = FileUtil.getFile(BasicRecord.SIG_PATH, getSigFileName());
        if(sigFile.exists())
            success = UploadDownloadFileUtil.uploadFile(context, "ECG", sigFile);

        if(success) {
            super.upload(context, callback);
        } else {
            callback.onFinish(RETURN_CODE_DOWNLOAD_ERR);
        }
    }

    @Override
    public void delete(Context context, ICodeCallback callback) {
        ICodeCallback cb = new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if(code==RETURN_CODE_SUCCESS) {
                    File sigFile = FileUtil.getFile(BasicRecord.SIG_PATH, getSigFileName());
                    if(sigFile.exists()) {
                        sigFile.delete();
                    }
                }
                callback.onFinish(code);
            }
        };
        super.delete(context, cb);
    }
}
