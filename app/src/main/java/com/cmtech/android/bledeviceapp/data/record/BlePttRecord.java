package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.PTT;

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
 * ClassName:      BlePttRecord
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2021/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BlePttRecord extends BasicRecord implements ISignalRecord, Serializable {
    //-----------------------------------------常量
    // 记录每个数据的字节数
    private static final int BYTES_PER_DATUM = 2;

    private int sampleRate = 0; // sample rate
    private int ecgCaliValue = 0; // ecg calibration value
    private int ppgCaliValue = 0; // ppg calibration value

    private BlePttRecord(String ver, long createTime, String devAddress, int creatorId) {
        super(PTT, ver, createTime, devAddress, creatorId);
    }

    // 创建信号文件
    public void createSigFile() {
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
        ecgCaliValue = json.getInt("ecgCaliValue");
        ppgCaliValue = json.getInt("ppgCaliValue");
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("ecgCaliValue", ecgCaliValue);
        json.put("ppgCaliValue", ppgCaliValue);
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
        throw new IllegalArgumentException();
    }

    public void setCaliValue(int caliValue) {
        throw new IllegalArgumentException();
    }

    public int getEcgCaliValue() {
        return ecgCaliValue;
    }

    public void setEcgCaliValue(int ecgCaliValue) {
        this.ecgCaliValue = ecgCaliValue;
    }

    public int getPpgCaliValue() {
        return ppgCaliValue;
    }

    public void setPpgCaliValue(int ppgCaliValue) {
        this.ppgCaliValue = ppgCaliValue;
    }

    @Override
    public int readData() throws IOException {
        if(sigFile == null) throw new IOException();
        return sigFile.readInt();
    }

    public boolean process(int ecg, int ppg) {
        boolean success = false;
        try {
            if(sigFile != null) {
                sigFile.writeShort((short) ecg);
                sigFile.writeShort((short) ppg);
                success = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public void download(Context context, ICodeCallback callback) {
        File file = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, getSigFileName());
        if(!file.exists()) {
            if(UploadDownloadFileUtil.isFileExist("PTT", getSigFileName())) {
                UploadDownloadFileUtil.downloadFile(context, "PTT", getSigFileName(), BasicRecord.SIG_FILE_PATH, new ICodeCallback() {
                    @Override
                    public void onFinish(int code) {
                        if(code==RETURN_CODE_SUCCESS) {
                            BlePttRecord.super.download(context, callback);
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
            if(!UploadDownloadFileUtil.isFileExist("PTT", getSigFileName())) {
                UploadDownloadFileUtil.uploadFile(context, "PTT", sigFile, new ICodeCallback() {
                    @Override
                    public void onFinish(int code) {
                        if (code == RETURN_CODE_SUCCESS) {
                            BlePttRecord.super.upload(context, callback);
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

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + ecgCaliValue + "-" + ppgCaliValue;
    }
}
