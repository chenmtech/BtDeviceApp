package com.cmtech.android.bledeviceapp.util;

import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Response;

import static com.cmtech.android.bledeviceapp.global.AppConstant.KMIC_URL;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_DATA_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;

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

    private static final String ACCOUNT_SERVLET_URL = "Account?";
    private static final String RECORD_SERVLET_URL = "Record?";
    private static final String APP_UPDATE_INFO_SERVLET_URL = "AppUpdateInfo?";

    public static WebResponse downloadNewestAppUpdateInfo() {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "download");

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestGet(KMIC_URL + APP_UPDATE_INFO_SERVLET_URL, data)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
            wResp.setContent(rtn.getJSONObject("appUpdateInfo"));
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    public static WebResponse signUp(Account account) {
        ViseLog.e("kmwebserviceutil signup");
        ViseLog.e(account);
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "signUp");
        data.put("userName", account.getUserName());
        data.put("password", account.getPassword());

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestGet(KMIC_URL + ACCOUNT_SERVLET_URL, data)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            ViseLog.e(respBody);
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    public static WebResponse login(Account account) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "login");
        data.put("userName", account.getUserName());
        data.put("password", account.getPassword());

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestGet(KMIC_URL + ACCOUNT_SERVLET_URL, data)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
            wResp.setContent(rtn.getInt("id"));
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    /*public static WebResponse signUporLogin(Account account) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "signUporLogin");
        data.put("platName", account.getPlatName());
        data.put("platId", account.getPlatId());

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestGet(KMIC_URL + ACCOUNT_SERVLET_URL, data)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }*/

    public static WebResponse uploadAccountInfo(Account account) {
        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        JSONObject json = account.toJson();
        try {
            json.put("cmd", "upload");
            json.put("id", account.getAccountId());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }

        try(Response response = HttpUtils.requestPost(KMIC_URL + ACCOUNT_SERVLET_URL, json)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
            //ViseLog.e("upload account code:" + wResp.getCode());
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    public static WebResponse downloadAccountInfo(Account account) {
        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "download");
            json.put("id", account.getAccountId());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }

        try(Response response = HttpUtils.requestPost(KMIC_URL + ACCOUNT_SERVLET_URL, json)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
            wResp.setContent(rtn.get("account"));
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    public static WebResponse queryRecordId(int recordTypeCode, long createTime, String devAddress, String ver) {
        Map<String, String> data = new HashMap<>();
        data.put("recordTypeCode", String.valueOf(recordTypeCode));
        data.put("createTime", String.valueOf(createTime));
        data.put("devAddress", devAddress);
        data.put("ver", ver);

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestGet(KMIC_URL + RECORD_SERVLET_URL, data)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
            wResp.setContent(rtn.getInt("id"));
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    public static WebResponse uploadRecord(int accountId, BasicRecord record) {
        JSONObject json;
        try {
            json = record.toJson();
            json.put("cmd", "upload");
            json.put("accountId", accountId);
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestPost(KMIC_URL + RECORD_SERVLET_URL, json)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    public static WebResponse downloadRecord(int accountId, BasicRecord record) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "download");
            json.put("accountId", accountId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("ver", record.getVer());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestPost(KMIC_URL + RECORD_SERVLET_URL, json)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
            wResp.setContent(rtn.get("record"));
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    public static WebResponse deleteRecord(int accountId, BasicRecord record) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "delete");
            json.put("accountId", accountId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("ver", record.getVer());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestPost(KMIC_URL + RECORD_SERVLET_URL, json)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    public static WebResponse downloadRecordList(int accountId, BasicRecord record, long fromTime, int num, String noteSearchStr) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "downloadList");
            json.put("accountId", accountId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("creatorId", record.getCreatorId());
            json.put("fromTime", fromTime);
            json.put("num", num);
            json.put("noteSearchStr", noteSearchStr);
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestPost(KMIC_URL + RECORD_SERVLET_URL, json)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
            wResp.setContent(rtn.get("records"));
            ViseLog.e(rtn);
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    public static WebResponse downloadReport(int accountId, BleEcgRecord record) {
        JSONObject json = new JSONObject();
        try {
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("cmd", "downloadReport");
            json.put("accountId", accountId);
            ViseLog.e(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestPost(KMIC_URL + RECORD_SERVLET_URL, json)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
            wResp.setContent(rtn.getJSONObject("reportResult"));
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    public static WebResponse requestReport(int accountId, BleEcgRecord record) {
        JSONObject json = new JSONObject();
        try {
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("cmd", "requestReport");
            json.put("accountId", accountId);
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }

        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestPost(KMIC_URL + RECORD_SERVLET_URL, json)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject rtn = new JSONObject(respBody);
            wResp.setCode(rtn.getInt("code"));
            wResp.setContent(rtn.getJSONObject("reportResult"));
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

/*
    public static void queryRecordId(int recordTypeCode, long createTime, String devAddress, String ver, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("recordTypeCode", String.valueOf(recordTypeCode));
        data.put("createTime", String.valueOf(createTime));
        data.put("devAddress", devAddress);
        data.put("ver", ver);
        HttpUtils.requestGet(KMURL + RECORD_SERVLET_URL, data, callback);
    }

    public static void uploadRecord(String platName, String platId, BasicRecord record, Callback callback) {
        try {
            JSONObject json = record.toJson();
            json.put("cmd", "upload");
            json.put("platName", platName);
            json.put("platId", platId);
            ViseLog.e(json.toString());
            HttpUtils.requestPost(KMURL + RECORD_SERVLET_URL, json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void downloadRecord(String platName, String platId, BasicRecord record, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "download");
            json.put("platName", platName);
            json.put("platId", platId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("ver", record.getVer());
            HttpUtils.requestPost(KMURL + RECORD_SERVLET_URL, json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void deleteRecord(String platName, String platId, BasicRecord record, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "delete");
            json.put("platName", platName);
            json.put("platId", platId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("ver", record.getVer());
            HttpUtils.requestPost(KMURL + RECORD_SERVLET_URL, json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void downloadRecordList(String platName, String platId, BasicRecord record, long fromTime, int num, String noteSearchStr, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "downloadList");
            json.put("platName", platName);
            json.put("platId", platId);
            json.put("recordTypeCode", record.getTypeCode());
            json.put("creatorPlat", record.getCreatorPlat());
            json.put("creatorId", record.getCreatorId());
            json.put("fromTime", fromTime);
            json.put("num", num);
            json.put("noteSearchStr", noteSearchStr);
            HttpUtils.requestPost(KMURL + RECORD_SERVLET_URL, json, callback);
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
            HttpUtils.requestPost(KMURL + RECORD_SERVLET_URL, json, callback);
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
            HttpUtils.requestPost(KMURL + RECORD_SERVLET_URL, json, callback);
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
            HttpUtils.requestPost(KMURL + RECORD_SERVLET_URL, json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    */
}
