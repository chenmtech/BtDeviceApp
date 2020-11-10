package com.cmtech.android.bledeviceapp.global;


import android.content.Context;

import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;

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
        account = LitePal.findFirst(Account.class);

        return isValid();
    }

    public void login(String userName, String password, final Context context, String showString, ICodeCallback callback) {
        Account acnt = new Account(userName, password);
        acnt.login(context, showString, code -> {
            if(code == RETURN_CODE_SUCCESS && acnt.getAccountId() != INVALID_ID) {
                this.account = acnt;
            }
            callback.onFinish(code);
        });
    }

    public void signUp(final Context context, String userName, String password, ICodeCallback callback) {
        Account account = new Account(userName, password);
        account.signUp(context, callback);
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
