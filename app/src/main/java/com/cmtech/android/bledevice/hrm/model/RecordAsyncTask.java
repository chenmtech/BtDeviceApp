package com.cmtech.android.bledevice.hrm.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.cmtech.android.bledevice.interf.AbstractRecord;
import com.cmtech.android.bledevice.interf.IRecord;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.KMWebService;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrm.model
 * ClassName:      RecordOpAsyncTask
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/20 上午6:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/20 上午6:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class RecordAsyncTask extends AsyncTask<AbstractRecord, Void, Boolean> {
    private ProgressDialog progressDialog;
    private final int cmd;

    public RecordAsyncTask(Context context, int cmd) {
        this.cmd = cmd;

        String pdTitle = "";
        String pdMessage = "";
        switch (cmd) {
            case 1:
                pdTitle = "保存记录";
                pdMessage = "保存记录中，请稍等";
                break;
            default:
                break;
        }

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(pdTitle);
        progressDialog.setMessage(pdMessage);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(AbstractRecord... records) {
        if(records == null) return false;

        AbstractRecord record = records[0];
        record.save();
        final boolean[] finish = new boolean[]{false, false};
        KMWebService.uploadRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), (BleEcgRecord10) record, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                finish[0] = true;
                finish[1] = false;
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String respBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(respBody);
                    final int code = json.getInt("code");
                    final String errStr = json.getString("errStr");
                    ViseLog.e(code+errStr);
                    finish[0] = true;
                    finish[1] = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        while (!finish[0]);

        return finish[1];
    }

    @Override
    protected void onPostExecute(Boolean result) {
        progressDialog.dismiss();

        if(result) {
            MyApplication.showMessageUsingShortToast("执行完毕");
        } else {
            MyApplication.showMessageUsingShortToast("执行失败");
        }
    }
}
