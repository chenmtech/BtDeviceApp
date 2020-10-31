package com.cmtech.android.bledeviceapp.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;

import static com.cmtech.android.bledeviceapp.data.record.IDiagnosable.CMD_DOWNLOAD_REPORT;
import static com.cmtech.android.bledeviceapp.data.record.IDiagnosable.CMD_REQUEST_REPORT;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;

public class ReportAsyncTask extends AsyncTask<BleEcgRecord, Void, WebResponse> {
    private final IWebResponseCallback callback;
    private final ProgressDialog progressDialog;
    private final int cmd;

    public ReportAsyncTask(Context context, int cmd, IWebResponseCallback callback) {
        this.callback = callback;
        this.cmd = cmd;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.wait_pls));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected WebResponse doInBackground(BleEcgRecord... ecgRecords) {
        WebResponse response = new WebResponse(RETURN_CODE_WEB_FAILURE, null);
        if(ecgRecords == null || ecgRecords.length == 0 || ecgRecords[0] == null) return response;

        BleEcgRecord record = ecgRecords[0];

        switch (cmd) {
            case CMD_REQUEST_REPORT:
                response = KMWebServiceUtil.requestReport(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record);
                break;
            case CMD_DOWNLOAD_REPORT:
                response = KMWebServiceUtil.downloadReport(MyApplication.getAccount().getPlatName(), MyApplication.getAccount().getPlatId(), record);
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