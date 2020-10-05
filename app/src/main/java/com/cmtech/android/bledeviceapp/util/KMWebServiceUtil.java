package com.cmtech.android.bledeviceapp.util;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledeviceapp.model.Account;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;

import static com.cmtech.android.bledeviceapp.global.AppConstant.KMURL;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      KMWebServiceUtil
 * Description:    康明网络服务
 * Author:         作者名
 * CreateDate:     2020/4/14 上午10:51
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/14 上午10:51
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class KMWebServiceUtil {
    public static final int RETURN_CODE_SUCCESS = 0; // web code success
    public static final int RETURN_CODE_WEB_FAILURE = 1; // web code failure
    public static final int RETURN_CODE_INVALID_PARA_ERR = 2;
    public static final int RETURN_CODE_SIGNUP_ERR = 3;
    public static final int RETURN_CODE_LOGIN_ERR = 4;
    public static final int RETURN_CODE_ACCOUNT_ERR = 5;
    public static final int RETURN_CODE_UPDATE_ERR = 6;
    public static final int RETURN_CODE_UPLOAD_ERR = 7;
    public static final int RETURN_CODE_DOWNLOAD_ERR = 8;
    public static final int RETURN_CODE_DELETE_ERR = 9;
    public static final int RETURN_CODE_OTHER_ERR = 10;

    private static final String ACCOUNT_OP_URL = "Account?";
    private static final String RECORD_OP_URL = "Record?";

    public static void signUp(String platName, String platId, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "signUp");
        data.put("platName", platName);
        data.put("platId", platId);
        HttpUtils.requestGet(KMURL + ACCOUNT_OP_URL, data, callback);
    }

    public static void login(String platName, String platId, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "login");
        data.put("platName", platName);
        data.put("platId", platId);
        HttpUtils.requestGet(KMURL + ACCOUNT_OP_URL, data, callback);
    }

    public static void signUporLogin(String platName, String platId, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "signUporLogin");
        data.put("platName", platName);
        data.put("platId", platId);
        HttpUtils.requestGet(KMURL + ACCOUNT_OP_URL, data, callback);
    }

    public static void uploadAccountInfo(Account account, Callback callback) {
        try {
            JSONObject json = account.toJson();
            json.put("cmd", "upload");
            ViseLog.e(json.toString());
            HttpUtils.requestPost(KMURL + ACCOUNT_OP_URL, json, callback);
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
            HttpUtils.requestPost(KMURL + ACCOUNT_OP_URL, json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void queryRecordId(int recordTypeCode, long createTime, String devAddress, String ver, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("recordTypeCode", String.valueOf(recordTypeCode));
        data.put("createTime", String.valueOf(createTime));
        data.put("devAddress", devAddress);
        data.put("ver", ver);
        HttpUtils.requestGet(KMURL + RECORD_OP_URL, data, callback);
    }

    public static void uploadRecord(String platName, String platId, IRecord record, Callback callback) {
        try {
            JSONObject json = record.toJson();
            json.put("cmd", "upload");
            json.put("platName", platName);
            json.put("platId", platId);
            ViseLog.e(json.toString());
            HttpUtils.requestPost(KMURL + RECORD_OP_URL, json, callback);
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
            json.put("ver", record.getVer());
            json.put("note", record.getNote());
            HttpUtils.requestPost(KMURL + RECORD_OP_URL, json, callback);
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
            json.put("ver", record.getVer());
            HttpUtils.requestPost(KMURL + RECORD_OP_URL, json, callback);
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
            json.put("ver", record.getVer());
            HttpUtils.requestPost(KMURL + RECORD_OP_URL, json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void downloadRecordBasicInfo(String platName, String platId, IRecord record, long fromTime, int num, String noteSearchStr, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "downloadBasicInfo");
            json.put("platName", platName);
            json.put("platId", platId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("fromTime", fromTime);
            json.put("creatorPlat", record.getCreatorPlat());
            json.put("creatorId", record.getCreatorId());
            json.put("num", num);
            json.put("noteSearchStr", noteSearchStr);
            HttpUtils.requestPost(KMURL + RECORD_OP_URL, json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void downloadReport(String platName, String platId, BleEcgRecord10 record, Callback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("cmd", "downloadReport");
            json.put("platName", platName);
            json.put("platId", platId);
            ViseLog.e(json.toString());
            HttpUtils.requestPost(KMURL + RECORD_OP_URL, json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void requestReport(String platName, String platId, BleEcgRecord10 record, Callback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("cmd", "requestReport");
            json.put("platName", platName);
            json.put("platId", platId);
            ViseLog.e(json.toString());
            HttpUtils.requestPost(KMURL + RECORD_OP_URL, json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void uploadReport(String platName, String platId, BleEcgRecord10 record, Callback callback) {
        try {
            JSONObject json = record.getReport().toJson();
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("cmd", "uploadReport");
            json.put("platName", platName);
            json.put("platId", platId);
            ViseLog.e(json.toString());
            HttpUtils.requestPost(KMURL + RECORD_OP_URL, json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
