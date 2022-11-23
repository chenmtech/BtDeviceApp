package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.PPG;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.UploadDownloadFileUtil;
import com.vise.utils.file.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      BlePpgRecord10
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2021/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BlePpgRecord extends BasicRecord implements ISignalRecord, Serializable {
    //-----------------------------------------常量
    // 记录每个数据的字节数
    private static final int BYTES_PER_DATUM = 2;

    private int sampleRate = 0; // sample rate
    private int gain = 0; // calibration value

    private BlePpgRecord(String ver, int accountId, long createTime, String devAddress) {
        super(PPG, ver, accountId, createTime, devAddress);
    }

    // 创建信号文件
    public void createSigFile() throws IOException{
        super.createSigFile(BYTES_PER_DATUM);
    }

    // 打开信号文件
    public void openSigFile() {
        super.openSigFile(BYTES_PER_DATUM);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        sampleRate = json.getInt("sampleRate");
        gain = json.getInt("gain");
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("gain", gain);
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
    public int getGain() {
        return gain;
    }

    public void setGain(int gain) {
        this.gain = gain;
    }

    @Override
    public int[] readData() throws IOException {
        if(sigFile == null) throw new IOException();
        return new int[]{sigFile.readShort()};
    }

    public boolean process(int ppg) {
        boolean success = false;
        try {
            if(sigFile != null) {
                sigFile.writeInt(ppg);
                success = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public void download(Context context, String showStr, ICodeCallback callback) {
        File file = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, getSigFileName());
        if(!file.exists()) {
            if(UploadDownloadFileUtil.isFileExist("PPG", getSigFileName())) {
                UploadDownloadFileUtil.downloadFile(context, "PPG", getSigFileName(), BasicRecord.SIG_FILE_PATH, new ICodeCallback() {
                    @Override
                    public void onFinish(int code, String msg) {
                        if(code== RCODE_SUCCESS) {
                            BlePpgRecord.super.download(context, showStr,  callback);
                        } else if(callback != null){
                            callback.onFinish(code, msg);
                        }
                    }
                });
            } else if(callback != null){
                callback.onFinish(RCODE_DATA_ERR, "记录已损坏");
            }
        } else {
            super.download(context, showStr, callback);
        }
    }

    @Override
    public void upload(Context context, ICodeCallback callback) {
        File sigFile = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, getSigFileName());
        if(sigFile.exists()) {
            if(!UploadDownloadFileUtil.isFileExist("PPG", getSigFileName())) {
                UploadDownloadFileUtil.uploadFile(context, "PPG", sigFile, new ICodeCallback() {
                    @Override
                    public void onFinish(int code, String msg) {
                        if (code == RCODE_SUCCESS) {
                            BlePpgRecord.super.upload(context, callback);
                        } else if(callback != null){
                            callback.onFinish(code, msg);
                        }
                    }
                });
            } else {
                super.upload(context, callback);
            }
        } else if(callback!=null){
            callback.onFinish(RCODE_DATA_ERR,"记录已损坏");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + gain;
    }
}
