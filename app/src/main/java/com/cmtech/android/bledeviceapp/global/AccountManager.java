package com.cmtech.android.bledeviceapp.global;


import android.content.Context;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;
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
    private Account account; // account

    AccountManager() {
    }

    public Account getAccount() {
        return account;
    }

    // account login in local client
    public boolean localLogin(String userName, String password) {
        Account account = LitePal.where("userName = ? and password = ?", userName, password).findFirst(Account.class);
        if(account != null) {
            this.account = account;
            return true;
        }
        return false;
    }

    public void webLogin(final Context context, String showString) {
        account.login(context, showString, code -> {
            if(code != RETURN_CODE_SUCCESS) {
                runOnUiThread(() -> Toast.makeText(context, R.string.login_failure, Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void webLogin(final Context context, String showString, ICodeCallback callback) {
        account.login(context, showString, callback);
    }

    public static void signUp(final Context context, String userName, String password) {
        Account.signUp(context, userName, password, code -> {
            if(code == RETURN_CODE_SUCCESS) {
                runOnUiThread(() -> Toast.makeText(context, "账户注册成功", Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() -> Toast.makeText(context, "账户注册失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // logout account
    public void localLogout(boolean remove) {
        if(!isLocalLogin()) return;

        if(remove) {
            account.remove();
        }

        account = null;
    }

    // is a valid account login
    public boolean isLocalLogin() {
        return account != null;
    }
}
