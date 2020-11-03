package com.cmtech.android.bledeviceapp.global;


import android.content.Context;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
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

    // login account in local client
    public void localLogin(String platName, String platId, String name, String icon) {
        Account account = LitePal.where("platName = ? and platId = ?", platName, platId).findFirst(Account.class);
        if(account == null) {
            account = new Account(platName, platId);
        }
        account.setName(name);
        account.setIcon(icon);
        account.save();
        this.account = account;
    }

    public void webLogin(final Context context) {
        if(!isLocalLogin()) return;

        account.signupOrLogin(context, code -> {
            if(code != RETURN_CODE_SUCCESS) {
                runOnUiThread(() -> Toast.makeText(context, R.string.login_failure, Toast.LENGTH_SHORT).show());
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
