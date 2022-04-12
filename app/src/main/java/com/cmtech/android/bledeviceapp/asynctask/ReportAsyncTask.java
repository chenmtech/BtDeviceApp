package com.cmtech.android.bledeviceapp.asynctask;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_WEB_FAILURE;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;

public class ReportAsyncTask extends AsyncTask<BleEcgRecord, Void, WebResponse> {
    private final IWebResponseCallback callback;
    private final ProgressDialog progressDialog;

    public ReportAsyncTask(Context context, IWebResponseCallback callback) {
        this.callback = callback;
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

        return KMWebServiceUtil.retrieveDiagnoseReport(MyApplication.getAccount(), record);
    }

    @Override
    protected void onPostExecute(WebResponse response) {
        callback.onFinish(response);
        if(progressDialog != null)
            progressDialog.dismiss();
    }
}