package com.cmtech.android.bledeviceapp.asynctask;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      UserWebAsyncTask
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/20 上午6:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/20 上午6:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class AccountAsyncTask extends AsyncTask<Account, Void, WebResponse> {
    public static final int CMD_UPLOAD_ACCOUNT = 1; // upload user info command
    public static final int CMD_DOWNLOAD_ACCOUNT = 2; // download user info command
    public static final int CMD_SIGNUP = 3; // sign up or login on the web
    public static final int CMD_LOGIN = 4;
    public static final int CMD_CHANGE_PASSWORD = 5;
    public static final int CMD_DOWNLOAD_SHARE_INFO = 6;
    public static final int CMD_CHANGE_SHARE_INFO = 7;
    public static final int CMD_ADD_SHARE_INFO = 8;
    public static final int CMD_DOWNLOAD_CONTACT_PEOPLE = 9;

    private final ProgressDialog progressDialog;
    private final int cmd;
    private final Object[] params;
    private final IWebResponseCallback callback;

    public AccountAsyncTask(Context context, String showString, int cmd, IWebResponseCallback callback) {
        this(context, showString, cmd, null, callback);
    }

    public AccountAsyncTask(Context context, String showString, int cmd, Object[] params, IWebResponseCallback callback) {
        this.cmd = cmd;
        this.callback = callback;
        this.params = params;

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
    protected WebResponse doInBackground(Account... accounts) {
        WebResponse response = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        if(accounts == null || accounts.length == 0 || accounts[0] == null) return response;

        Account account = accounts[0];
        switch (cmd) {
            // UPLOAD
            case CMD_UPLOAD_ACCOUNT:
                response = KMWebServiceUtil.uploadAccountInfo(account);
                break;

            // DOWNLOAD
            case CMD_DOWNLOAD_ACCOUNT:
                response = KMWebServiceUtil.downloadAccountInfo(account);
                break;

            case CMD_SIGNUP:
                response = KMWebServiceUtil.signUp(account);
                break;

            case CMD_LOGIN:
                response = KMWebServiceUtil.login(account);
                break;

            case CMD_CHANGE_PASSWORD:
                response = KMWebServiceUtil.changePassword(account);
                break;

            case CMD_DOWNLOAD_SHARE_INFO:
                response = KMWebServiceUtil.downloadShareInfo(account);
                break;

            case CMD_CHANGE_SHARE_INFO:
                int fromId = (Integer) params[0];
                int status = (Integer) params[1];
                response = KMWebServiceUtil.changeShareInfo(account, fromId, status);
                break;

            case CMD_ADD_SHARE_INFO:
                int toId = (Integer) params[0];
                response = KMWebServiceUtil.addShare(account, toId);
                break;

            case CMD_DOWNLOAD_CONTACT_PEOPLE:
                int contactId = (Integer) params[0];
                response = KMWebServiceUtil.downloadContactPerson(account, contactId);
                break;

            default:
                break;
        }

        return response;
    }

    @Override
    protected void onPostExecute(WebResponse response) {
        callback.onFinish(response);

        if(progressDialog != null)
            progressDialog.dismiss();
    }
}
