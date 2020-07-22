package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;

import static com.cmtech.android.bledeviceapp.AppConstant.KMURL;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      KMWebService
 * Description:    康明网络服务
 * Author:         作者名
 * CreateDate:     2020/4/14 上午10:51
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/14 上午10:51
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class KMWebService {
    public static void signUp(String platName, String platId, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "signUp");
        data.put("platName", platName);
        data.put("platId", platId);
        HttpUtils.requestGet(KMURL + "Account?", data, callback);
    }

    public static void login(String platName, String platId, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "login");
        data.put("platName", platName);
        data.put("platId", platId);
        HttpUtils.requestGet(KMURL + "Account?", data, callback);
    }

    public static void signUporLogin(String platName, String platId, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "signUporLogin");
        data.put("platName", platName);
        data.put("platId", platId);
        HttpUtils.requestGet(KMURL + "Account?", data, callback);
    }

    public static void uploadAccountInfo(Account account, Callback callback) {
        try {
            JSONObject json = account.toJson();
            json.put("cmd", "upload");
            ViseLog.e(json.toString());
            HttpUtils.requestPost(KMURL + "Account?", json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void downloadAccountInfo(Account account, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "download");
            json.put("platName", account.getPlatName());
            json.put("platId", account.getPlatId());
            HttpUtils.requestPost(KMURL + "Account?", json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void queryRecord(int recordTypeCode, long createTime, String devAddress, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("recordTypeCode", String.valueOf(recordTypeCode));
        data.put("createTime", String.valueOf(createTime));
        data.put("devAddress", devAddress);
        HttpUtils.requestGet(KMURL + "Record?", data, callback);
    }

    public static void uploadRecord(String platName, String platId, IRecord record, Callback callback) {
        try {
            JSONObject json = record.toJson();
            json.put("cmd", "upload");
            json.put("platName", platName);
            json.put("platId", platId);
            ViseLog.e(json.toString());
            HttpUtils.requestPost(KMURL + "Record?", json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void updateRecordNote(String platName, String platId, IRecord record, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "updateNote");
            json.put("platName", platName);
            json.put("platId", platId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("note", record.getNote());
            HttpUtils.requestPost(KMURL + "Record?", json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void deleteRecord(String platName, String platId, IRecord record, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "delete");
            json.put("platName", platName);
            json.put("platId", platId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            HttpUtils.requestPost(KMURL + "Record?", json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void downloadRecordBasicInfo(String platName, String platId, IRecord record, int num, String noteFilterStr, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "downloadInfo");
            json.put("platName", platName);
            json.put("platId", platId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("fromTime", record.getCreateTime());
            json.put("creatorPlat", record.getCreatorPlat());
            json.put("creatorId", record.getCreatorId());
            json.put("num", num);
            json.put("noteFilterStr", noteFilterStr);
            HttpUtils.requestPost(KMURL + "Record?", json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void downloadRecord(String platName, String platId, IRecord record, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "download");
            json.put("platName", platName);
            json.put("platId", platId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            HttpUtils.requestPost(KMURL + "Record?", json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
