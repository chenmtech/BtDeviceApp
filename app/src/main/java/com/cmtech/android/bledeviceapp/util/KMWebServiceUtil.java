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
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      KMWebServiceUtil
 * Description:    康明网络服务
 * 只负责打包数据，发送http网络请求，并将接收到的JSON解包为WebResponse响应，包括code和content
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
        return processGetRequest(KMIC_URL + APP_UPDATE_INFO_SERVLET_URL, data);
    }

    public static WebResponse signUp(Account account) {
        //ViseLog.e("sign up: "+account);
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "signUp");
        data.put("userName", account.getUserName());
        data.put("password", MD5Utils.getMD5Code(account.getPassword()));
        return processGetRequest(KMIC_URL + ACCOUNT_SERVLET_URL, data);
    }

    public static WebResponse login(Account account) {
        //ViseLog.e("login: " + account);
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "login");
        data.put("userName", account.getUserName());
        data.put("password", MD5Utils.getMD5Code(account.getPassword()));
        return processGetRequest(KMIC_URL + ACCOUNT_SERVLET_URL, data);
    }

    public static WebResponse changePassword(Account account) {
        //ViseLog.e("change password: "+account);
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "changePassword");
        data.put("userName", account.getUserName());
        data.put("password", MD5Utils.getMD5Code(account.getPassword()));
        return processGetRequest(KMIC_URL + ACCOUNT_SERVLET_URL, data);
    }

    public static WebResponse uploadAccountInfo(Account account) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json = account.toJson();
        try {
            json.put("cmd", "upload");
            json.put("id", account.getAccountId());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }
        //ViseLog.e(json);
        return processPostRequest(KMIC_URL + ACCOUNT_SERVLET_URL, json);
    }

    public static WebResponse downloadAccountInfo(Account account) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "download");
            json.put("id", account.getAccountId());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }
        //ViseLog.e(json);
        return processPostRequest(KMIC_URL + ACCOUNT_SERVLET_URL, json);
    }

    public static WebResponse queryRecordId(int recordTypeCode, long createTime, String devAddress, String ver) {
        Map<String, String> data = new HashMap<>();
        data.put("recordTypeCode", String.valueOf(recordTypeCode));
        data.put("createTime", String.valueOf(createTime));
        data.put("devAddress", devAddress);
        data.put("ver", ver);
        return processGetRequest(KMIC_URL + RECORD_SERVLET_URL, data);
    }

    public static WebResponse uploadRecord(Account account, BasicRecord record) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json;
        try {
            json = record.toJson();
            json.put("cmd", "upload");
            json.put("accountId", account.getAccountId());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    public static WebResponse downloadRecord(Account account, BasicRecord record) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "download");
            json.put("accountId", account.getAccountId());
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("ver", record.getVer());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    public static WebResponse deleteRecord(Account account, BasicRecord record) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "delete");
            json.put("accountId", account.getAccountId());
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("ver", record.getVer());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    public static WebResponse downloadRecordList(Account account, BasicRecord record, long fromTime, int num, String noteSearchStr) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "downloadList");
            json.put("accountId", account.getAccountId());
            json.put("recordTypeCode", record.getTypeCode());
            json.put("creatorId", record.getCreatorId());
            json.put("fromTime", fromTime);
            json.put("num", num);
            json.put("noteSearchStr", noteSearchStr);
        } catch (JSONException e) {
            ViseLog.e(e);
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    public static WebResponse downloadReport(Account account, BleEcgRecord record) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "downloadReport");
            json.put("accountId", account.getAccountId());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            ViseLog.e(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    public static WebResponse requestReport(Account account, BleEcgRecord record) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "requestReport");
            json.put("accountId", account.getAccountId());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    // 账户登录
    private static WebResponse accountWebLogin(Account account) {
        if(account == null) return new WebResponse(RETURN_CODE_DATA_ERR, null);
        if(account.isNeedWebLogin()) {
            WebResponse response = KMWebServiceUtil.login(account);
            if(response.getCode() == RETURN_CODE_SUCCESS) {
                account.setNeedWebLogin(false);
            }
            return response;
        } else
            return new WebResponse(RETURN_CODE_SUCCESS, null);
    }

    // 处理GET请求
    private static WebResponse processGetRequest(String url, Map<String, String> data) {
        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestGet(url, data)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject returnJson = new JSONObject(respBody);
            int code = returnJson.getInt("code");
            Object content = (returnJson.has("content")) ? returnJson.get("content") : null;
            wResp.setCode(code);
            wResp.setContent(content);
        } catch (JSONException e) {
            e.printStackTrace();
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

    // 处理POST请求
    private static WebResponse processPostRequest(String url, JSONObject json) {
        WebResponse wResp = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        try(Response response = HttpUtils.requestPost(url, json)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            //ViseLog.e(respBody);
            JSONObject returnJson = new JSONObject(respBody);
            int code = returnJson.getInt("code");
            Object content = (returnJson.has("content")) ? returnJson.get("content") : null;
            wResp.setCode(code);
            wResp.setContent(content);
        } catch (JSONException e) {
            ViseLog.e("processPostRequest:" + e);
            wResp.setCode(RETURN_CODE_DATA_ERR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wResp;
    }

/*

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
