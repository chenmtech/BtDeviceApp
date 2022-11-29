package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.AppVersionUtils;

public class AppUpdateManager {
    private final AppPackageInfo appUpdateInfo;

    public AppUpdateManager() {
        appUpdateInfo = new AppPackageInfo();
    }

    /**
     * 获取APP更新信息
     * @param context
     * @param callback
     */
    public void retrieveAppUpdateInfo(Context context, ICodeCallback callback) {
        appUpdateInfo.download(context, null, callback);
    }

    public AppPackageInfo getAppUpdateInfo() {
        return appUpdateInfo;
    }

    /**
     * 是否存在更新
     * @return
     */
    public boolean existUpdate() {
        int curVerCode = AppVersionUtils.getVersionCode();
        return curVerCode < appUpdateInfo.getVerCode();
    }

    public void updateApp(Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String msgBuilder =
                "当前版本：" + AppVersionUtils.getVerName() + "\n" +
                "新版本：" + appUpdateInfo.getVerName() + "\n" +
                "更新内容：" + appUpdateInfo.getNote() + "\n" +
                "安装包大小：" + appUpdateInfo.getSize() + "MB";
        builder.setTitle("App存在更新").setMessage(msgBuilder);
        builder.setCancelable(false);
        builder.setPositiveButton("立刻升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadAndInstallApk(context);
            }
        });
        builder.setNegativeButton("暂不升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int curVerCode = AppVersionUtils.getVersionCode();
                if(curVerCode < appUpdateInfo.getSupportedVerCode()) {
                    Toast.makeText(context, "当前版本太低，不升级将无法正常使用", Toast.LENGTH_SHORT).show();
                } else
                    dialog.dismiss();
            }
        });
    }

    private void downloadAndInstallApk(Context context) {
        appUpdateInfo.downloadApkFileAndInstall(context);
    }
}
