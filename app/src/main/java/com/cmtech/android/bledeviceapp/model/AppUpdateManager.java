package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.util.APKVersionCodeUtils;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class AppUpdateManager {
    private final AppUpdateInfo updateInfo = new AppUpdateInfo();

    public AppUpdateManager() {

    }

    public void retrieveUpdateInfo(Context context) {
        updateInfo.download(context, new IWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if(code == RETURN_CODE_SUCCESS) {
                    //ViseLog.e(updateInfo);
                    if(isNeedUpdate())
                        updateApp(context);
                }
            }
        });
    }

    private void updateApp(Context context) {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("新版本：").append(updateInfo.getVerName()).append("\n");
        msgBuilder.append("更新信息：").append(updateInfo.getNote()).append("\n");
        msgBuilder.append("安装包大小：").append(updateInfo.getSize()).append("MB");
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("请更新应用程序").setMessage(msgBuilder.toString());
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadApk(context, updateInfo.getUrl());
            }
        }).setNegativeButton("", null).show();
    }

    private void downloadApk(Context context, String url) {
        updateInfo.downApkFile(context);
    }

    private boolean isNeedUpdate() {
        int currentVerCode = APKVersionCodeUtils.getVersionCode();
        return currentVerCode < updateInfo.getVerCode();
    }
}
