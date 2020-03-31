package com.cmtech.android.bledeviceapp.model;


import com.vise.log.ViseLog;

import org.litepal.LitePal;


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
    private static AccountManager instance; // singleton instance
    private Account account; // account

    private AccountManager() {
    }

    public static AccountManager getInstance() {
        if (instance == null) {
            synchronized (AccountManager.class) {
                if (instance == null) {
                    instance = new AccountManager();
                }
            }
        }
        return instance;
    }

    public Account getAccount() {
        return account;
    }
    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isSignIn() {
        return account != null;
    }

    public void signOut() {
        account = null;
    }

}
