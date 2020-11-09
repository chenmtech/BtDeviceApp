package com.cmtech.android.bledeviceapp.global;


import android.content.Context;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

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
    public boolean localLogin() {
        Account account = LitePal.findFirst(Account.class);
        //ViseLog.e(account);
        if(account == null) return false;
        this.account = account;
        return account.getAccountId() != INVALID_ID;
    }

/*    public void login(final Context context, String showString) {
        if(account == null) return;
        account.login(context, showString, code -> {
            if(code != RETURN_CODE_SUCCESS) {
                runOnUiThread(() -> Toast.makeText(context, R.string.login_failure, Toast.LENGTH_SHORT).show());
            }
        });
    }*/

    public void login(String userName, String password, final Context context, String showString, ICodeCallback callback) {
        Account acnt = new Account(userName, password);
        acnt.login(context, showString, code -> {
            if(code == RETURN_CODE_SUCCESS && acnt.getAccountId() != INVALID_ID) {
                this.account = acnt;
            }
            callback.onFinish(code);
        });
    }

    public void signUp(final Context context, String userName, String password) {
        Account account = new Account(userName, password);
        account.signUp(context, code -> {
            ViseLog.e("code:"+code);
            if(code == RETURN_CODE_SUCCESS) {
                Toast.makeText(context, "账户注册成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, WebFailureHandler.handle(code), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // logout account
    public void localLogout(boolean remove) {
        if(account != null && remove) {
            account.remove();
            account = null;
        }
    }

    // is a valid account login
    public boolean isValid() {
        return account != null && (account.getAccountId() != INVALID_ID);
    }
}
