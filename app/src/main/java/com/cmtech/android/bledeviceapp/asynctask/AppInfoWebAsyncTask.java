package com.cmtech.android.bledeviceapp.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.AppPackageInfo;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;

public class AppInfoWebAsyncTask extends AsyncTask<AppPackageInfo, Void, WebResponse> {
    public static final int CMD_DOWNLOAD_INFO = 0;
    public static final int CMD_DOWNLOAD_APK = 1;

    private final int cmd;
    private final IWebResponseCallback callback;
    private final ProgressDialog pBar;

    public AppInfoWebAsyncTask(Context context, int cmd, IWebResponseCallback callback) {
        this.cmd = cmd;
        this.callback = callback;

        if(cmd == CMD_DOWNLOAD_APK) {
            pBar = new ProgressDialog(context);
            pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pBar.setCancelable(false);
            pBar.setMessage("正在下载安装包，请稍后...");
            pBar.setProgress(0);
        } else {
            pBar = null;
        }
    }

    @Override
    protected void onPreExecute() {
        if(pBar != null) {
            pBar.show();
        }
    }

    @Override
    protected WebResponse doInBackground(AppPackageInfo... infos) {
        return KMWebServiceUtil.downloadNewestAppUpdateInfo();
    }

    @Override
    protected void onPostExecute(WebResponse response) {
        if(pBar != null) {
            pBar.dismiss();
        }
        if(callback != null)
            callback.onFinish(response);
    }
}
