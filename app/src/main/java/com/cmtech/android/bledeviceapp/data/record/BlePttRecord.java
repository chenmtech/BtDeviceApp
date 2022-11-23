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
public class BlePttRecord extends BasicRecord implements Serializable {
    //-----------------------------------------常量
    private BlePttRecord(String ver, int accountId, long createTime, String devAddress) {
        super(PTT, ver, accountId, createTime, devAddress, 2, 2, new String[]{"mV","unknown"});
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        return json;
    }

    @Override
    public int[] readData() throws IOException {
        if(sigFile == null) throw new IOException();
        return sigFile.readShort();
    }

    public boolean process(int ecg, int ppg) {
        boolean success = false;
        try {
            if(sigFile != null) {
                sigFile.writeShort(new short[]{(short) ecg, (short) ppg});
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
            if(UploadDownloadFileUtil.isFileExist("PTT", getSigFileName())) {
                UploadDownloadFileUtil.downloadFile(context, "PTT", getSigFileName(), BasicRecord.SIG_FILE_PATH, new ICodeCallback() {
                    @Override
                    public void onFinish(int code, String msg) {
                        if(code== RCODE_SUCCESS) {
                            BlePttRecord.super.download(context, showStr, callback);
                        } else if(callback!=null){
                            callback.onFinish(code, msg);
                        }
                    }
                });
            } else if(callback!=null){
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
            if(!UploadDownloadFileUtil.isFileExist("PTT", getSigFileName())) {
                UploadDownloadFileUtil.uploadFile(context, "PTT", sigFile, new ICodeCallback() {
                    @Override
                    public void onFinish(int code, String msg) {
                        if (code == RCODE_SUCCESS) {
                            BlePttRecord.super.upload(context, callback);
                        } else if(callback!=null){
                            callback.onFinish(code, msg);
                        }
                    }
                });
            } else {
                super.upload(context, callback);
            }
        } else if(callback!=null){
            callback.onFinish(RCODE_DATA_ERR, "记录已损坏");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
