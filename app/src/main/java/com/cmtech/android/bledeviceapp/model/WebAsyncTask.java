package com.cmtech.android.bledeviceapp.model;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_DATA_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_ADD_SHARE_INFO;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_CHANGE_PASSWORD;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_CHANGE_SHARE_INFO;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_DELETE_RECORD;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_DOWNLOAD_ACCOUNT;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_DOWNLOAD_APP_INFO;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_DOWNLOAD_CONTACT_PEOPLE;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_DOWNLOAD_RECORD;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_DOWNLOAD_RECORDS;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_DOWNLOAD_SHARE_INFO;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_LOGIN;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_QUERY_RECORD_ID;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_RETRIEVE_DIAGNOSE_REPORT;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_SHARE_RECORD;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_SIGNUP;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_UPLOAD_ACCOUNT;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_UPLOAD_RECORD;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;

import java.util.Date;
import java.util.List;

/**
 * ClassName:      WebAsyncTask
 * Description:    执行网络异步操作的AsyncTask类
 * Author:         chenm
 * CreateDate:     2020/4/20 上午6:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/20 上午6:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class WebAsyncTask extends AsyncTask<Object, Void, WebResponse> {
    private static final int DEFAULT_RECORD_DOWNLOAD_NUM = 10; // default download record num per time

    private final ProgressDialog progressDialog;
    private final int cmd;
    private final Object[] params;
    private final IWebResponseCallback callback;

    public WebAsyncTask(Context context, String showString, int cmd, IWebResponseCallback callback) {
        this(context, showString, cmd, null, callback);
    }

    public WebAsyncTask(Context context, String showString, int cmd, Object[] params, IWebResponseCallback callback) {
        super();

        this.cmd = cmd;
        this.params = params;
        this.callback = callback;

        if(!TextUtils.isEmpty(showString)) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(showString);
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
            return new WebResponse(RETURN_CODE_DATA_ERR, null);

        WebResponse response = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        BasicRecord record = null;
        Account account = null;

        switch (cmd) {
            // QUERY ID
            case CMD_QUERY_RECORD_ID:
                record = (BasicRecord) objects[0];
                response = KMWebServiceUtil.queryRecordId(record.getTypeCode(), record.getCreateTime(), record.getDevAddress(), record.getVer());
                break;

            // UPLOAD
            case CMD_UPLOAD_RECORD:
                record = (BasicRecord) objects[0];
                response = KMWebServiceUtil.uploadRecord(MyApplication.getAccount(), record);
                break;

            // DOWNLOAD
            case CMD_DOWNLOAD_RECORD:
                record = (BasicRecord) objects[0];
                response = KMWebServiceUtil.downloadRecord(MyApplication.getAccount(), record);
                break;

            // DELETE
            case CMD_DELETE_RECORD:
                record = (BasicRecord) objects[0];
                response = KMWebServiceUtil.deleteRecord(MyApplication.getAccount(), record);
                break;

            // DOWNLOAD RECORD LIST
            case CMD_DOWNLOAD_RECORDS:
                record = (BasicRecord) objects[0];
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
                response = KMWebServiceUtil.downloadRecords(MyApplication.getAccount(), record, filterTime, downloadNum, filterStr);
                break;

            case CMD_SHARE_RECORD:
                record = (BasicRecord) objects[0];
                int shareId = (Integer) params[0];
                response = KMWebServiceUtil.shareRecord(MyApplication.getAccount(), record, shareId);
                break;

            // UPLOAD
            case CMD_UPLOAD_ACCOUNT:
                account = (Account) objects[0];
                response = KMWebServiceUtil.uploadAccountInfo(account);
                break;

            // DOWNLOAD
            case CMD_DOWNLOAD_ACCOUNT:
                account = (Account) objects[0];
                response = KMWebServiceUtil.downloadAccountInfo(account);
                break;

            case CMD_SIGNUP:
                account = (Account) objects[0];
                response = KMWebServiceUtil.signUp(account);
                break;

            case CMD_LOGIN:
                account = (Account) objects[0];
                response = KMWebServiceUtil.login(account);
                break;

            case CMD_CHANGE_PASSWORD:
                account = (Account) objects[0];
                response = KMWebServiceUtil.changePassword(account);
                break;

            case CMD_DOWNLOAD_SHARE_INFO:
                account = (Account) objects[0];
                response = KMWebServiceUtil.downloadShareInfo(account);
                break;

            case CMD_CHANGE_SHARE_INFO:
                account = (Account) objects[0];
                int fromId = (Integer) params[0];
                int status = (Integer) params[1];
                response = KMWebServiceUtil.changeShareInfo(account, fromId, status);
                break;

            case CMD_ADD_SHARE_INFO:
                account = (Account) objects[0];
                int toId = (Integer) params[0];
                response = KMWebServiceUtil.addShareInfo(account, toId);
                break;

            case CMD_DOWNLOAD_CONTACT_PEOPLE:
                account = (Account) objects[0];
                List<Integer> contactIds = (List<Integer>) params[0];
                response = KMWebServiceUtil.downloadContactPeople(account, contactIds);
                break;

            case CMD_RETRIEVE_DIAGNOSE_REPORT:
                record = (BasicRecord) objects[0];
                response = KMWebServiceUtil.retrieveDiagnoseReport(MyApplication.getAccount(), record);
                break;

            case CMD_DOWNLOAD_APP_INFO:
                response = KMWebServiceUtil.downloadNewestAppUpdateInfo();
                break;

            default:
                break;
        }

        return response;
    }

    @Override
    protected void onPostExecute(WebResponse response) {
        if(callback != null)
            callback.onFinish(response);

        if(progressDialog != null)
            progressDialog.dismiss();
    }
}
