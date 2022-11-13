package com.cmtech.android.bledeviceapp.util;

import static com.cmtech.android.bledeviceapp.global.AppConstant.KMIC_URL;
import static com.cmtech.android.bledeviceapp.global.AppConstant.SUPPORT_RECORD_TYPES;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_DATA_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_WEB_FAILURE;

import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Response;

/**
 * ClassName:      WebService11Util
 * Description:    提供网络服务的类，服务协议版本号为1.1,包括账号，记录等的网络操作
 *                 只负责打包数据，发送http网络请求，并将接收到的JSON数据解包为WebResponse响应，包括code和content
 * Author:         chenm
 * CreateDate:     2020/4/14 上午10:51
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/14 上午10:51
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class WebService11Util {
    //-----------------------------------------------静态常量
    // 缺省每次要下载的记录个数
    public static final int DEFAULT_RECORD_DOWNLOAD_NUM = 10;

    // 网络协议版本号
    private static final String SVER = "1.1";

    //----------------用于访问网络端不同servlet的后缀
    //account servlet 后缀
    private static final String ACCOUNT_SERVLET_URL = "Account?";

    // record servlet 后缀
    private static final String RECORD_SERVLET_URL = "Record?";

    // APP update info servlet 后缀
    private static final String APP_UPDATE_INFO_SERVLET_URL = "AppUpdateInfo?";


    //----------------下面是网络操作的命令----------------------//
    // 查询一条记录的ID
    public static final int CMD_QUERY_RECORD_ID = 1;
    // 上传一条记录
    public static final int CMD_UPLOAD_RECORD = 2;
    // 下载一条记录
    public static final int CMD_DOWNLOAD_RECORD = 3;
    // 删除一条记录
    public static final int CMD_DELETE_RECORD = 4;
    // 下载多条记录
    public static final int CMD_DOWNLOAD_RECORDS = 5;
    // 分享一条记录
    public static final int CMD_SHARE_RECORD = 6;
    // 获取一条记录的诊断报告
    public static final int CMD_RETRIEVE_DIAGNOSE_REPORT = 7;
    // 上传账户信息
    public static final int CMD_UPLOAD_ACCOUNT = 8;
    // 下载账户信息
    public static final int CMD_DOWNLOAD_ACCOUNT = 9;
    // 注册账户
    public static final int CMD_SIGNUP = 10;
    // 登录账户
    public static final int CMD_LOGIN = 11;
    // 重置账户密码
    public static final int CMD_RESET_PASSWORD = 12;
    // 下载账户联系人信息，仅包括发起者ID，接收者ID，以及申请状态
    public static final int CMD_DOWNLOAD_CONTACT_INFO = 13;
    // 下载联系人的一些账户信息，包括账户ID，昵称，简介和头像
    public static final int CMD_DOWNLOAD_CONTACT_ACCOUNT_INFO = 14;
    // 添加一条申请联系人信息
    public static final int CMD_ADD_CONTACT = 15;
    // 同意一条联系人申请信息
    public static final int CMD_AGREE_CONTACT = 16;
    // 删除一条联系人信息
    public static final int CMD_DELETE_CONTACT = 17;
    // 下载APP更新信息
    public static final int CMD_DOWNLOAD_APP_INFO = 18;
    // 下载APK文件
    public static final int CMD_DOWNLOAD_APK = 19;

    //--------------------------------------------------------静态函数
    //---------使用Get Request网络操作------------//
    // 查询记录的ID号
    public static WebResponse queryRecordId(Account account, BasicRecord record) {
        Map<String, String> data = new HashMap<>();
        data.put("sver", SVER);
        data.put("cmd", String.valueOf(CMD_QUERY_RECORD_ID));
        data.put("accountId", String.valueOf(account.getAccountId()));
        data.put("recordTypeCode", String.valueOf(record.getTypeCode()));
        data.put("createTime", String.valueOf(record.getCreateTime()));
        data.put("devAddress", record.getDevAddress());
        data.put("ver", record.getVer());
        return processGetRequest(KMIC_URL + RECORD_SERVLET_URL, data);
    }

    // 下载最新的app更新信息
    public static WebResponse downloadAppUpdateInfo() {
        Map<String, String> data = new HashMap<>();
        data.put("sver", SVER);
        data.put("cmd", String.valueOf(CMD_DOWNLOAD_APP_INFO));
        return processGetRequest(KMIC_URL + APP_UPDATE_INFO_SERVLET_URL, data);
    }

    // 注册新账户
    public static WebResponse signUp(Account account) {
        Map<String, String> data = new HashMap<>();
        data.put("sver", SVER);
        data.put("cmd", String.valueOf(CMD_SIGNUP));
        data.put("userName", account.getUserName());
        data.put("password", MD5Utils.getMD5Code(account.getPassword()));
        return processGetRequest(KMIC_URL + ACCOUNT_SERVLET_URL, data);
    }

    // 账户登录
    public static WebResponse login(Account account) {
        Map<String, String> data = new HashMap<>();
        data.put("sver", SVER);
        data.put("cmd", String.valueOf(CMD_LOGIN));
        data.put("userName", account.getUserName());
        data.put("password", MD5Utils.getMD5Code(account.getPassword()));
        return processGetRequest(KMIC_URL + ACCOUNT_SERVLET_URL, data);
    }

    // 重置密码
    public static WebResponse resetPassword(Account account) {
        Map<String, String> data = new HashMap<>();
        data.put("sver", SVER);
        data.put("cmd", String.valueOf(CMD_RESET_PASSWORD));
        data.put("userName", account.getUserName());
        data.put("password", MD5Utils.getMD5Code(account.getPassword()));
        return processGetRequest(KMIC_URL + ACCOUNT_SERVLET_URL, data);
    }

    //---------使用Post Request网络操作------------//
    // 上传账户信息
    public static WebResponse uploadAccountInfo(Account account) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json;
        try {
            json = account.toJson();
            json.put("sver", SVER);
            json.put("cmd", CMD_UPLOAD_ACCOUNT);
            json.put("accountId", account.getAccountId());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + ACCOUNT_SERVLET_URL, json);
    }

    // 下载账户信息
    public static WebResponse downloadAccountInfo(Account account) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_DOWNLOAD_ACCOUNT);
            json.put("accountId", account.getAccountId());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + ACCOUNT_SERVLET_URL, json);
    }

    // 下载联系人信息
    public static WebResponse downloadContactInfo(Account account) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_DOWNLOAD_CONTACT_INFO);
            json.put("accountId", account.getAccountId());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + ACCOUNT_SERVLET_URL, json);
    }

    // 下载联系人账户信息
    public static WebResponse downloadContactAccountInfo(Account account, List<Integer> contactIds) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_DOWNLOAD_CONTACT_ACCOUNT_INFO);
            json.put("accountId", account.getAccountId());
            json.put("contactIds", ListStringUtil.listToString(contactIds));
            ViseLog.e(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + ACCOUNT_SERVLET_URL, json);
    }

    // 添加联系人
    public static WebResponse addContact(Account account, int contactId) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_ADD_CONTACT);
            json.put("accountId", account.getAccountId());
            json.put("contactId", contactId);
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + ACCOUNT_SERVLET_URL, json);
    }

    // 同意联系人的申请
    public static WebResponse agreeContact(Account account, int contactId) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_AGREE_CONTACT);
            json.put("accountId", account.getAccountId());
            json.put("contactId", contactId);
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + ACCOUNT_SERVLET_URL, json);
    }

    // 删除联系人
    public static WebResponse deleteContact(Account account, int contactId) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_DELETE_CONTACT);
            json.put("accountId", account.getAccountId());
            json.put("contactId", contactId);
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + ACCOUNT_SERVLET_URL, json);
    }

    // 上传记录
    public static WebResponse uploadRecord(Account account, BasicRecord record) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json;
        try {
            json = record.toJson();
            json.put("sver", SVER);
            json.put("cmd", CMD_UPLOAD_RECORD);
            json.put("accountId", account.getAccountId());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    // 给联系人分享一条记录
    public static WebResponse shareRecord(Account account, BasicRecord record, int contactId) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_SHARE_RECORD);
            json.put("accountId", account.getAccountId());
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("contactId", contactId);
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    // 下载一条记录
    public static WebResponse downloadRecord(Account account, BasicRecord record) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_DOWNLOAD_RECORD);
            json.put("accountId", account.getAccountId());
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("ver", record.getVer());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    // 删除一条记录
    public static WebResponse deleteRecord(Account account, BasicRecord record) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_DELETE_RECORD);
            json.put("accountId", account.getAccountId());
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            json.put("ver", record.getVer());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    /**
     * 下载记录列表
     * @param account 账户
     * @param recordType 保存要下载的记录类型
     * @param fromTime 起始时间
     * @param num 记录个数
     * @param filterStr 滤波字符串
     * @return 网络响应
     */
    public static WebResponse downloadRecords(Account account, RecordType recordType, long fromTime, int num, String filterStr) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_DOWNLOAD_RECORDS);
            json.put("accountId", account.getAccountId());

            StringBuilder builder = new StringBuilder();
            if(recordType == RecordType.ALL) {
                for(RecordType t : SUPPORT_RECORD_TYPES) {
                    if(t != RecordType.ALL) {
                        builder.append(t.getCode()).append(",");
                    }
                }
            } else {
                builder.append(recordType.getCode());
            }
            json.put("recordTypeCode", builder.toString());
            //json.put("creatorId", record.getCreatorId());
            json.put("fromTime", fromTime);
            json.put("num", num);
            json.put("filterStr", filterStr);
        } catch (JSONException e) {
            ViseLog.e(e);
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    // 获取记录的诊断报告
    public static WebResponse retrieveDiagnoseReport(Account account, BasicRecord record) {
        WebResponse resp = accountWebLogin(account);
        if(resp.getCode() != RCODE_SUCCESS) return resp;

        JSONObject json = new JSONObject();
        try {
            json.put("sver", SVER);
            json.put("cmd", CMD_RETRIEVE_DIAGNOSE_REPORT);
            json.put("accountId", account.getAccountId());
            json.put("recordTypeCode", record.getTypeCode());
            json.put("createTime", record.getCreateTime());
            json.put("devAddress", record.getDevAddress());
            ViseLog.e(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);
        }
        return processPostRequest(KMIC_URL + RECORD_SERVLET_URL, json);
    }

    // 账户登录
    private static WebResponse accountWebLogin(Account account) {
        if(account == null) return new WebResponse(RCODE_DATA_ERR, "账户错误", null);
        if(!account.isWebLoginSuccess()) {
            WebResponse response = WebService11Util.login(account);
            if(response.getCode() == RCODE_SUCCESS) {
                account.setWebLoginSuccess(true);
            }
            return response;
        } else
            return new WebResponse(RCODE_SUCCESS, "登录成功", null);
    }

    // 处理GET请求
    private static WebResponse processGetRequest(String url, Map<String, String> data) {
        WebResponse wResp = new WebResponse(RCODE_WEB_FAILURE, "网络连接失败", null);
        try(Response response = HttpUtils.requestGet(url, data)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject respJson = new JSONObject(respBody);
            int code = respJson.getInt("code");
            String msg = respJson.getString("message");
            Object content = (respJson.has("content")) ? respJson.get("content") : null;
            wResp.set(code, msg, content);
        } catch (JSONException | IOException e) {
            wResp.set(RCODE_DATA_ERR, "数据错误", null);
        }
        return wResp;
    }

    // 处理POST请求
    private static WebResponse processPostRequest(String url, JSONObject json) {
        WebResponse wResp = new WebResponse(RCODE_WEB_FAILURE, "网络连接失败", null);
        try(Response response = HttpUtils.requestPost(url, json)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            JSONObject respJson = new JSONObject(respBody);
            int code = respJson.getInt("code");
            String msg = respJson.getString("message");
            Object content = (respJson.has("content")) ? respJson.get("content") : null;
            wResp.set(code, msg, content);
        } catch (JSONException | IOException e) {
            wResp.set(RCODE_DATA_ERR, "数据错误", null);
        }
        return wResp;
    }
}
