package com.cmtech.android.bledeviceapp.model;


import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.util.List;


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
    private static AccountManager instance; // 单例
    private Account account; // 用户

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
    // 是否已经登录
    public boolean isSignIn() {
        return account != null;
    }
    // 退出账号
    public void signOut() {
        account = null;
    }

    // 注册新账户
    public boolean signUp(String phone) {
        Account find = LitePal.where("phone = ?", phone).findFirst(Account.class);

        if(find != null) {
            ViseLog.e("The account exists.");
            return false;
        } else {
            account = new Account();
            account.setPhone(phone);
            account.save();
            return true;
        }
    }

    // 登录
    public boolean signIn(String phone) {
        Account account = LitePal.where("phone = ?", phone).findFirst(Account.class);
        if(account != null) {
            this.account = account;
            return true;
        }
        return false;
    }

}
