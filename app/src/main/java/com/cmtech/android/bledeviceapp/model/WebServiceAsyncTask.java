package com.cmtech.android.bledeviceapp.model;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_DATA_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_WEB_FAILURE;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_ADD_CONTACT;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_AGREE_CONTACT;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_RESET_PASSWORD;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DELETE_CONTACT;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DELETE_RECORD;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DOWNLOAD_ACCOUNT;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DOWNLOAD_APP_INFO;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DOWNLOAD_CONTACT_ACCOUNT_INFO;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DOWNLOAD_CONTACT_INFO;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DOWNLOAD_RECORD;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DOWNLOAD_RECORDS;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_LOGIN;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_QUERY_RECORD_ID;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_RETRIEVE_DIAGNOSE_REPORT;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_SHARE_RECORD;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_SIGNUP;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_UPLOAD_ACCOUNT;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_UPLOAD_RECORD;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.DEFAULT_RECORD_DOWNLOAD_NUM;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.util.WebServiceUtil;

import java.util.Date;
import java.util.List;

/**
 * ClassName:      WebServiceAsyncTask
 * Description:    执行网络服务异步任务的AsyncTask类。
 *                 这个类的作用主要是帮助WebServiceUtil类完成三个任务：
 *                 1、异步执行WebServiceUtil中的任务；2、解析输入参数；3、在主线程执行IWebResponseCallback回调
 * Author:         chenm
 * CreateDate:     2020/4/20 上午6:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/20 上午6:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class WebServiceAsyncTask extends AsyncTask<Object, Void, WebResponse> {
    // 旋转进度条
    private final ProgressDialog progressDialog;

    // 网络服务命令
    private final int cmd;

    // 额外的参数
    private final Object[] params;

    // 网络响应回调
    private final IWebResponseCallback wRespCb;

    public WebServiceAsyncTask(Context context, String showStr, int cmd, IWebResponseCallback wRespCb) {
        this(context, showStr, cmd, null, wRespCb);
    }

    /**
     * 创建一个网络服务异步任务
     * @param context 上下文
     * @param showText 在进度条上显示的文本，如果为空，则不显示进度条
     * @param cmd 网络服务命令
     * @param params 额外参数
     * @param wRespCb 网络响应回调
     */
    public WebServiceAsyncTask(Context context, String showText, int cmd, Object[] params, IWebResponseCallback wRespCb) {
        super();

        this.cmd = cmd;
        this.params = params;
        this.wRespCb = wRespCb;

        if(!TextUtils.isEmpty(showText)) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(showText);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        } else {
            progressDialog = null;
        }
    }

    @Override
    protected void onPreExecute() {
        if(progressDialog != null)
            progressDialog.show();
    }

    @Override
    protected WebResponse doInBackground(Object... objects) {
        if(objects == null || objects.length == 0 || objects[0] == null)
            return new WebResponse(RCODE_DATA_ERR, "数据错误", null);

        WebResponse response = new WebResponse(RCODE_WEB_FAILURE, "网络连接失败", null);
        BasicRecord record;
        Account account;

        switch (cmd) {
            // QUERY ID
            case CMD_QUERY_RECORD_ID:
                record = (BasicRecord) objects[0];
                response = WebServiceUtil.queryRecordId(MyApplication.getAccount(), record);
                break;

            // UPLOAD
            case CMD_UPLOAD_RECORD:
                record = (BasicRecord) objects[0];
                response = WebServiceUtil.uploadRecord(MyApplication.getAccount(), record);
                break;

            // DOWNLOAD
            case CMD_DOWNLOAD_RECORD:
                record = (BasicRecord) objects[0];
                response = WebServiceUtil.downloadRecord(MyApplication.getAccount(), record);
                break;

            // DELETE
            case CMD_DELETE_RECORD:
                record = (BasicRecord) objects[0];
                response = WebServiceUtil.deleteRecord(MyApplication.getAccount(), record);
                break;

            // DOWNLOAD RECORD LIST
            case CMD_DOWNLOAD_RECORDS:
                record = (BasicRecord) objects[0];
                RecordType type = record.getType();
                int downloadNum = 0;
                String filterStr = "";
                long filterTime = new Date().getTime();
                if(params == null || params.length == 0) {
                    downloadNum = DEFAULT_RECORD_DOWNLOAD_NUM;
                } else if(params.length == 1) {
                    downloadNum = (Integer) params[0];
                } else if(params.length == 2) {
                    downloadNum = (Integer) params[0];
                    filterStr = (String) params[1];
                } else if(params.length == 3) {
                    downloadNum = (Integer) params[0];
                    filterStr = (String) params[1];
                    filterTime = (Long) params[2];
                }
                response = WebServiceUtil.downloadRecords(MyApplication.getAccount(), type, filterTime, downloadNum, filterStr);
                break;

            // 分享记录
            case CMD_SHARE_RECORD:
                record = (BasicRecord) objects[0];
                int shareContactId = (Integer) params[0];
                response = WebServiceUtil.shareRecord(MyApplication.getAccount(), record, shareContactId);
                break;

            // 获取记录的诊断报告
            case CMD_RETRIEVE_DIAGNOSE_REPORT:
                record = (BasicRecord) objects[0];
                response = WebServiceUtil.retrieveDiagnoseReport(MyApplication.getAccount(), record);
                break;

            // 上传账户信息
            case CMD_UPLOAD_ACCOUNT:
                account = (Account) objects[0];
                response = WebServiceUtil.uploadAccountInfo(account);
                break;

            // 下载账户信息
            case CMD_DOWNLOAD_ACCOUNT:
                account = (Account) objects[0];
                response = WebServiceUtil.downloadAccountInfo(account);
                break;

            // 注册新账户
            case CMD_SIGNUP:
                account = (Account) objects[0];
                response = WebServiceUtil.signUp(account);
                break;

            // 账户登录
            case CMD_LOGIN:
                account = (Account) objects[0];
                response = WebServiceUtil.login(account);
                break;

            // 重置密码
            case CMD_RESET_PASSWORD:
                account = (Account) objects[0];
                response = WebServiceUtil.resetPassword(account);
                break;

            // 下载账户联系人信息
            case CMD_DOWNLOAD_CONTACT_INFO:
                account = (Account) objects[0];
                response = WebServiceUtil.downloadContactInfo(account);
                break;

            // 下载联系人的公开账户信息
            case CMD_DOWNLOAD_CONTACT_ACCOUNT_INFO:
                account = (Account) objects[0];
                List<Integer> contactIds = (List<Integer>) params[0];
                response = WebServiceUtil.downloadContactAccountInfo(account, contactIds);
                break;

            // 批准联系人申请
            case CMD_AGREE_CONTACT:
                account = (Account) objects[0];
                int agreeContactId = (Integer) params[0];
                response = WebServiceUtil.agreeContact(account, agreeContactId);
                break;

            // 申请新的联系人
            case CMD_ADD_CONTACT:
                account = (Account) objects[0];
                int addContactId = (Integer) params[0];
                response = WebServiceUtil.addContact(account, addContactId);
                break;

            // 删除一个联系人
            case CMD_DELETE_CONTACT:
                account = (Account) objects[0];
                int deleteContactId = (Integer) params[0];
                response = WebServiceUtil.deleteContact(account, deleteContactId);
                break;

            // 下载app更新信息
            case CMD_DOWNLOAD_APP_INFO:
                response = WebServiceUtil.downloadAppUpdateInfo();
                break;

            default:
                break;
        }

        return response;
    }

    @Override
    protected void onPostExecute(WebResponse response) {
        if(wRespCb != null)
            wRespCb.onFinish(response);

        if(progressDialog != null)
            progressDialog.dismiss();
    }
}
