package com.cmtech.android.bledeviceapp.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;

import java.util.Date;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_DATA_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      RecordWebAsyncTask
 * Description:    执行记录的网络操作的AsyncTask类
 * Author:         作者名
 * CreateDate:     2020/4/20 上午6:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/20 上午6:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class RecordAsyncTask extends AsyncTask<BasicRecord, Void, WebResponse> {
    public static final int CMD_QUERY_RECORD_ID = 1; // query record command ID
    public static final int CMD_UPLOAD_RECORD = 2; // upload record command
    public static final int CMD_DOWNLOAD_RECORD = 3; // download record command
    public static final int CMD_DELETE_RECORD = 4; // delete record command
    public static final int CMD_DOWNLOAD_RECORD_LIST = 5; // download basic record information command

    private static final int DEFAULT_DOWNLOAD_NUM = 10; // default download record num per time

    private final ProgressDialog progressDialog;
    private final int cmd;
    private final Object[] params;
    private final IWebResponseCallback callback;

    public RecordAsyncTask(Context context, String showString, int cmd, IWebResponseCallback callback) {
        this(context, showString, cmd, null, callback);
    }

    public RecordAsyncTask(Context context, String showString, int cmd, Object[] params, IWebResponseCallback callback) {
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
    protected WebResponse doInBackground(BasicRecord... records) {
        if(records == null || records.length == 0) return new WebResponse(RETURN_CODE_DATA_ERR, null);

        WebResponse response = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        BasicRecord record = records[0];

        switch (cmd) {
            // QUERY ID
            case CMD_QUERY_RECORD_ID:
                response = KMWebServiceUtil.queryRecordId(record.getTypeCode(), record.getCreateTime(), record.getDevAddress(), record.getVer());
                break;

            // UPLOAD
            case CMD_UPLOAD_RECORD:
                response = KMWebServiceUtil.uploadRecord(MyApplication.getAccount(), record);
                break;

            // DOWNLOAD
            case CMD_DOWNLOAD_RECORD:
                response = KMWebServiceUtil.downloadRecord(MyApplication.getAccount(), record);
                break;

            // DELETE
            case CMD_DELETE_RECORD:
                response = KMWebServiceUtil.deleteRecord(MyApplication.getAccount(), record);
                break;

            // DOWNLOAD RECORD LIST
            case CMD_DOWNLOAD_RECORD_LIST:
                int downloadNum = 0;
                String filterStr = "";
                long fromTime = new Date().getTime();
                if(params == null || params.length == 0) {
                    downloadNum = DEFAULT_DOWNLOAD_NUM;
                } else if(params.length == 1) {
                    downloadNum = (Integer) params[0];
                } else if(params.length == 2) {
                    downloadNum = (Integer) params[0];
                    filterStr = (String) params[1];
                } else if(params.length == 3) {
                    downloadNum = (Integer) params[0];
                    filterStr = (String) params[1];
                    fromTime = (Long) params[2];
                }
                response = KMWebServiceUtil.downloadBasicRecords(MyApplication.getAccount(), record, fromTime, downloadNum, filterStr);
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
