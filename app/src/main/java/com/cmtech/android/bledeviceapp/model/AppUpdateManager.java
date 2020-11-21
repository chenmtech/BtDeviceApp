package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.util.APKVersionCodeUtils;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class AppUpdateManager {
    private final AppInfo updateInfo = new AppInfo();

    public AppUpdateManager() {

    }

    public void retrieveUpdateInfo(Context context) {
        updateInfo.download(context, (code) -> {
            if(code == RETURN_CODE_SUCCESS) {
                if(needUpdate())
                    updateApp(context);
            } else {
                Toast.makeText(context, WebFailureHandler.handle(code), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateApp(Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String msgBuilder = "新版本号：" + updateInfo.getVerName() + "\n" +
                "更新内容：" + updateInfo.getNote() + "\n" +
                "安装包大小：" + updateInfo.getSize() + "MB";
        builder.setTitle("请更新应用程序").setMessage(msgBuilder);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadApk(context);
            }
        }).show();
    }

    private void downloadApk(Context context) {
        updateInfo.downloadApkFileThenInstall(context);
    }

    private boolean needUpdate() {
        int currentVerCode = APKVersionCodeUtils.getVersionCode();
        return currentVerCode < updateInfo.getVerCode();
    }
}
