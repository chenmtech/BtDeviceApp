package com.cmtech.android.bledeviceapp.util;

import static com.cmtech.android.bledeviceapp.global.AppConstant.KMIC_URL;
import static com.cmtech.android.bledeviceapp.global.AppConstant.SUPPORT_RECORD_TYPES;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_DATA_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;

import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
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

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      KMWebServiceUtil
 * Description:    提供康明网络服务的类，包括账号，记录等的网络操作
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

    // 账号注册
    private static final String CMD_SIGNUP = "signUp";

    // 账号登录
    private static final String CMD_LOGIN = "login";

    // 修改账号密码
    private static final String CMD_CHANGE_PASSWORD = "changePassword";

    // 下载
    private static final String CMD_DOWNLOAD = "download";

    // 上传
    private static final String CMD_UPLOAD = "upload";

    // 删除
    private static final String CMD_DELETE = "delete";

    // 下载记录列表
    private static final String CMD_DOWNLOAD_RECORD_LIST = "downloadRecordList";

    // 获取记录的诊断报告
    private static final String CMD_RETRIEVE_DIAGNOSE_REPORT = "retrieveDiagnoseReport";

    public static WebResponse downloadNewestAppUpdateInfo() {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", CMD_DOWNLOAD);
        return processGetRequest(KMIC_URL + APP_UPDATE_INFO_SERVLET_URL, data);
    }

    public static WebResponse signUp(Account account) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", CMD_SIGNUP);
        data.put("userName", account.getUserName());
        data.put("password", MD5Utils.getMD5Code(account.getPassword()));
        return processGetRequest(KMIC_URL + ACCOUNT_SERVLET_URL, data);
    }

    public static WebResponse login(Account account) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", CMD_LOGIN);
        data.put("userName", account.getUserName());
        data.put("password", MD5Utils.getMD5Code(account.getPassword()));
        return processGetRequest(KMIC_URL + ACCOUNT_SERVLET_URL, data);
    }

    public static WebResponse changePassword(Account account) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", CMD_CHANGE_PASSWORD);
        data.put("userName", account.getUserName());
        data.put("password", MD5Utils.getMD5Code(account.getPassword()));
        return processGetRequest(KMIC_URL + ACCOUNT_SERVLET_URL, data);
    }

    public static WebResponse uploadAccountInfo(Account account) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json = account.toJson();
        try {
            json.put("cmd", CMD_UPLOAD);
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
            json.put("cmd", CMD_DOWNLOAD);
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
            json.put("cmd", CMD_UPLOAD);
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
            json.put("cmd", CMD_DOWNLOAD);
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
            json.put("cmd", CMD_DELETE);
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

    public static WebResponse downloadRecordList(Account account, BasicRecord record, long fromTime, int num, String filterStr) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("cmd", CMD_DOWNLOAD_RECORD_LIST);
            json.put("accountId", account.getAccountId());

            RecordType type = record.getType();
            StringBuilder builder = new StringBuilder();
            if(type == RecordType.ALL) {
                for(RecordType t : SUPPORT_RECORD_TYPES) {
                    if(t != RecordType.ALL) {
                        builder.append(t.getCode()).append(",");
                    }
                }
            } else {
                builder.append(type.getCode());
            }
            json.put("recordTypeCode", builder.toString());
            json.put("creatorId", record.getCreatorId());
            json.put("fromTime", fromTime);
            json.put("num", num);
            json.put("filterStr", filterStr);
        } catch (JSONException e) {
            ViseLog.e(e);
            return new WebResponse(RETURN_CODE_DATA_ERR, null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    public static WebResponse retrieveDiagnoseReport(Account account, BleEcgRecord record) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RETURN_CODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("cmd", CMD_RETRIEVE_DIAGNOSE_REPORT);
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

}
