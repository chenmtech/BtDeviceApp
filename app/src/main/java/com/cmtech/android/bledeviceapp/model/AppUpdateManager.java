package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.APKVersionCodeUtils;

public class AppUpdateManager {
    private final AppPackageInfo appInfo;

    public AppUpdateManager() {
        appInfo = new AppPackageInfo();
    }

    public void retrieveAppInfo(Context context, ICodeCallback callback) {
        appInfo.download(context, null, callback);
    }

    public boolean needUpdate() {
        int currentVerCode = APKVersionCodeUtils.getVersionCode();
        return currentVerCode < appInfo.getVerCode();
    }

    public void updateApp(Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String msgBuilder = "新版本号：" + appInfo.getVerName() + "\n" +
                "更新内容：" + appInfo.getNote() + "\n" +
                "安装包大小：" + appInfo.getSize() + "MB";
        builder.setTitle("请更新应用程序").setMessage(msgBuilder);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadAndInstallApk(context);
            }
        });
        builder.setNegativeButton("暂不升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void downloadAndInstallApk(Context context) {
        appInfo.downloadApkFileAndInstall(context);
    }
}
