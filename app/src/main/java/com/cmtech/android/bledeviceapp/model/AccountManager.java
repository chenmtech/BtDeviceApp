package com.cmtech.android.bledeviceapp.model;


import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.KMWebServiceUtil;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledeviceapp.AppConstant.PHONE_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.AppConstant.QQ_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.AppConstant.WX_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_SUCCESS;
import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

/**
  *
  * ClassName:      AccountManager
  * Description:    账户管理器
  * Author:         chenm
  * CreateDate:     2018/10/27 上午4:01
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/20 上午4:01
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class AccountManager {
    private static final AccountManager INSTANCE = new AccountManager();
    private Account account; // account

    private AccountManager() {
    }

    public static AccountManager getInstance() {
        return INSTANCE;
    }

    public Account getAccount() {
        return account;
    }

    // login account
    public void login(String platName, String platId, String name, String icon) {
        Account account = LitePal.where("platName = ? and platId = ?", platName, platId).findFirst(Account.class);
        if(account == null) {
            account = new Account(platName, platId, name, "", icon);
        } else {
            account.setName(name);
            account.setIcon(icon);
        }
        account.save();
        this.account = account;
    }

    public void webLogin(final Context context) {
        if(!isLogin()) return;

        KMWebServiceUtil.signUporLogin(account.getPlatName(), account.getPlatId(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.web_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String respBody = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject json = new JSONObject(respBody);
                    int code = json.getInt("code");
                    ViseLog.e("login/sign up:"+code);
                    if(code == WEB_CODE_SUCCESS) {
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(() -> Toast.makeText(context, R.string.login_failure, Toast.LENGTH_SHORT).show());
            }
        });
    }

    // logout account
    public void logout(boolean isRemoved) {
        if(!isLogin()) return;

        if(isRemoved) {
            if(account.getPlatName().equals(QQ_PLAT_NAME)) {
                Platform plat = ShareSDK.getPlatform(QQ.NAME);
                plat.removeAccount(true);
            } else if(account.getPlatName().equals(WX_PLAT_NAME)) {
                Platform plat = ShareSDK.getPlatform(Wechat.NAME);
                plat.removeAccount(true);
            } else if(account.getPlatName().equals(PHONE_PLAT_NAME)) {
                PhoneAccount.removeAccount();
            }

            if(!TextUtils.isEmpty(account.getIcon())) {
                try {
                    FileUtil.deleteFile(new File(account.getIcon()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        account = null;
    }

    // is a valid account login
    public boolean isLogin() {
        return account != null;
    }
}
