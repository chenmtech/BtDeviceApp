package com.cmtech.android.bledeviceapp.model;


import android.content.Context;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.CODE_SUCCESS;
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
    private static User account; // account

    private AccountManager() {
    }

    public static User getAccount() {
        return account;
    }

    // login account
    public static void login(String platName, String platId, String name, String icon) {
        User account = LitePal.where("platName = ? and platId = ?", platName, platId).findFirst(User.class);
        if(account == null) {
            account = new User(platName, platId, name, icon);
        } else {
            account.setName(name);
            account.setIcon(icon);
        }
        account.save();
        AccountManager.account = account;
    }

    public static void loginKMServer(final Context context) {
        if(!isLogin()) return;

        KMWebService.signUporLogin(account.getPlatName(), account.getPlatId(), new Callback() {
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
                if(response.body() == null) return;
                String respBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(respBody);
                    int code = json.getInt("code");
                    if(code != CODE_SUCCESS) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, R.string.login_failure, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    ViseLog.e("login/sign up:"+code);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // logout account
    public static void logout() {
        account = null;
    }

    // is a valid account login
    public static boolean isLogin() {
        return account != null;
    }

    // clear account's local icon
    public static void clearLocalIcon() {
        if(account != null) {
            account.setIcon("");
            account.save();
        }
    }
}
